(ns fireworks.prof
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh]))

(defonce ^:public events (atom []))
(def     ^:public mode   (atom nil))    ; nil | :sequential | :nested
(def     ^:public term-cols (atom nil))
(defn set-width! [n] (reset! term-cols n))

(defn enable! [m]
  {:pre [(#{:sequential :nested} m)]}
  (reset! mode m))

(defn disable! [] (reset! mode nil))
(defn clear!   [] (reset! events []))

(def ^:dynamic *stack* [])

;; ── checkpoint mode (laps) ──
(defn checkpoint [& path]
  (when (= :sequential @mode)
    (swap! events conj {:kind :mark :path (vec path) :t (System/nanoTime)}))
  nil)

(defn boundary! []
  (when (= :sequential @mode)
    (swap! events conj {:kind :mark :path ::boundary :t (System/nanoTime)}))
  nil)

;; ── prof mode (spans) ──
(defmacro prof [label & body]
  `(if (= :nested @mode)
     (binding [*stack* (conj *stack* ~label)]
       (let [start# (System/nanoTime)
             ret#   (do ~@body)
             ms#    (/ (double (- (System/nanoTime) start#)) 1e6)]
         (swap! events conj {:kind :span :path *stack* :ms ms#})
         ret#))
     (do ~@body)))

;; ── report ──
(defn- mark-intervals []
  (->> @events
       (filter #(= :mark (:kind %)))
       (partition 2 1)
       (map (fn [[a b]]
              {:path (:path b)
               :ms   (/ (double (- (:t b) (:t a))) 1e6)}))
       (remove #(= ::boundary (:path %)))))

(defn- span-samples []
  (filter #(= :span (:kind %)) @events))

(defn- aggregate [samples]
  (let [order (reduce (fn [m p]
                        (let [p (vec p)]
                          (reduce (fn [m i]
                                    (let [pre (subvec p 0 (inc i))]
                                      (if (contains? m pre) m (assoc m pre (count m)))))
                                  m (range (count p)))))
                      {} (map :path samples))]
    (->> samples
         (group-by :path)
         (map (fn [[path xs]]
                (let [ms (map :ms xs)]
                  {:path path :n (count ms) :sum (reduce + ms)
                   :mean (/ (reduce + ms) (count ms))
                   :min (apply min ms) :max (apply max ms)})))
         (sort-by (fn [{:keys [path]}]
                    (let [p (vec path)]
                      (->> (range (count p))
                           (map #(format "%04d" (order (subvec p 0 (inc %)))))
                           (str/join "\u0000"))))))))

(defn- fmt-dur [ms]
  (let [nanos (* ms 1e6)]
    (cond
      (>= nanos 1e9) (format "%ds"  (Math/round (/ nanos 1e9)))
      (>= nanos 1e6) (format "%dms" (Math/round (/ nanos 1e6)))
      (>= nanos 1e3) (format "%dµs" (Math/round (/ nanos 1e3)))
      :else          (format "%dns" (Math/round (double nanos))))))


;; helpers for mean-bar mode

(def ^:private max-bar 40)   ; longest bar, in chars, for the largest mean

(defn- parse-long* [s]
  (try (Long/parseLong (str/trim (str s))) (catch Exception _ nil)))

#_(defn- term-width []
  (or (parse-long* (System/getenv "COLUMNS"))
      (try (let [r (sh/sh "bash" "-c" "stty size < /dev/tty")]
             (when (zero? (:exit r))
               (-> r :out str/trim (str/split #"\s+") second parse-long*)))
           (catch Exception _ nil))
      (try (let [r (sh/sh "bash" "-c" "tput cols < /dev/tty")]
             (when (zero? (:exit r)) (parse-long* (:out r))))
           (catch Exception _ nil))
      80))

(defn- term-width []
  (or @term-cols 80))

;; (defn- pad [s w] (str s (apply str (repeat (max 0 (- w (count s))) " "))))

;; (defn- print-mean-bar [samples]
;;   (let [width (term-width)
;;         items (aggregate samples)
;;         maxv  (apply max (map :mean items))
;;         cells (map (fn [{:keys [path mean]}]
;;                      (let [label (str (last path))
;;                            val   (fmt-dur mean)
;;                            bl    (max 1 (Math/round (* (/ mean maxv) (double max-bar))))
;;                            bar   (apply str (repeat bl "▒"))]
;;                        {:label label :val val :bar bar
;;                         :w (max bl (count label) (count val))}))
;;                    items)
;;         ;; pack cells left-to-right, wrap when the next won't fit the terminal
;;         rows  (loop [cs cells, row [], used 0, out []]
;;                 (if (empty? cs)
;;                   (conj out row)
;;                   (let [{:keys [w] :as c} (first cs)
;;                         need (+ w (if (seq row) 1 0))]
;;                     (if (and (seq row) (> (+ used need) width))
;;                       (recur cs [] 0 (conj out row))
;;                       (recur (rest cs) (conj row c) (+ used need) out)))))]
;;     (doseq [row rows]
;;       (println)
;;       (println (str/join " " (map #(pad (:label %) (:w %)) row)))
;;       (println (str/join " " (map #(pad (:val %)   (:w %)) row)))
;;       (println (str/join " " (map #(pad (:bar %)   (:w %)) row))))))


;; ── tree-bar layout for :mean-bar (nested mode only) ──

(defn- build-tree
  "Flat aggregate items (each with vector :path) -> root nodes with :children,
   preserving first-appearance order at each level."
  [items]
  (let [by-path  (into {} (map (juxt (comp vec :path) identity)) items)
        children (fn [pp]
                   (->> items
                        (filter #(and (= (inc (count pp)) (count (:path %)))
                                      (= pp (vec (butlast (:path %))))))
                        (map (comp vec :path))))]
    (letfn [(node [p] (assoc (by-path p) :children (mapv node (children p))))]
      (->> items
           (filter #(= 1 (count (:path %))))
           (map (comp vec :path))
           (mapv node)))))

(defn- bar-len [mean maxv max-bar]
  (max 1 (Math/round (* (/ mean maxv) (double max-bar)))))

;; cell width is just the bar — label & timing are clipped to it
(defn- layout-nodes
  [nodes maxv x0 bound depth max-bar]
  (let [gap   1
        nats  (mapv #(bar-len (:mean %) maxv max-bar) nodes)
        total (+ (reduce + nats) (* gap (max 0 (dec (count nodes)))))
        scale (if (and bound (> total bound) (pos? total))
                (/ (double bound) total)
                1.0)]
    (loop [ns nodes, ws nats, x x0, out []]
      (if (empty? ns)
        out
        (let [node   (first ns)
              barw   (max 1 (int (Math/floor (* (first ws) scale))))
              placed (assoc node :x x :barw barw :depth depth)
              kids   (if (< depth 2)                       ; cap: render levels 0,1,2
                       (layout-nodes (:children node) maxv x barw (inc depth) max-bar)
                       [])
              ]
          (recur (rest ns) (rest ws) (+ x barw gap)
                 (into (conj out placed) kids)))))))


(def ^:private rev "\u001b[7m")
(def ^:private rst "\u001b[0m")

(defn- clip-padded
  "Optional 1-space left pad + text, space-filled to exactly `width`, clipped."
  [text width left-pad?]
  (let [s (if left-pad? (str " " text) (str text))]
    (if (>= (count s) width)
      (subs s 0 width)
      (str s (apply str (repeat (- width (count s)) " "))))))

(defn- emit-label-line [row]
  (let [sb (StringBuilder.)]
    (doseq [{:keys [x label-text barw]} (sort-by :x row)]
      (while (< (.length sb) x) (.append sb \space))
      (.append sb (clip-padded label-text barw false)))   ; no left pad
    (.toString sb)))

(defn- emit-bar-line [row]
  (let [sb  (StringBuilder.)
        vis (volatile! 0)]
    (doseq [{:keys [x val barw]} (sort-by :x row)]
      (while (< @vis x) (.append sb \space) (vswap! vis inc))
      (let [content (clip-padded val barw true)]          ; 1-space left pad
        (.append sb rev) (.append sb content) (.append sb rst)
        (vswap! vis + (count content))))
    (.toString sb)))

(defn- find-node
  "Find the node whose :path equals start-path, anywhere in the tree."
  [nodes start-path]
  (some (fn [n]
          (if (= (:path n) start-path)
            n
            (find-node (:children n) start-path)))
        nodes))

(defn- print-mean-bar [samples start-path]
  (let [items   (aggregate samples)
        maxv    (apply max (map :mean items))
        width   (term-width)
        tree    (build-tree items)
        roots   (if (seq start-path)
                  (if-let [n (find-node tree (vec start-path))]
                    [n]
                    nil)
                  tree)]
    (if (nil? roots)
      (println (format "(no node at start-path %s — top-level paths: %s)"
                       (pr-str start-path)
                       (pr-str (map :path tree))))
      (let [gaps    (max 0 (dec (count roots)))
            sum-m   (reduce + (map :mean roots))
            max-bar (max 1 (Math/round (* (/ (- width gaps) (double sum-m)) maxv)))
            placed  (->> (layout-nodes roots maxv 0 width 0 max-bar)
                         (map (fn [n] (assoc n
                                             :label-text (str (last (:path n)))
                                             :val (fmt-dur (:mean n))))))
            max-d   (apply max (map :depth placed))]
        (doseq [d (range (inc max-d))]
          (let [row (filter #(= d (:depth %)) placed)]
            (println)
            (println (emit-label-line row))
            (println (emit-bar-line row))))))))



(defn report
  ([] (report :full nil))
  ([style] (report style nil))
  ([style start-path]
   (let [m @mode
         samples (case m :sequential (mark-intervals) :nested (span-samples) nil nil)]
     (if (nil? samples)
       (println "\n(no active profiling mode)")
       (do
         (println (format "\n── profile [%s] ──" (name m)))
         (cond
           (= style :mean-bar)
           (if (= m :nested)
             (print-mean-bar samples start-path)
             (println "(mean-bar requires :nested mode)"))

           :else
           (doseq [{:keys [path n sum mean min max]} (aggregate samples)]
             (let [indent (apply str (repeat (dec (count path)) "  "))
                   label  (str indent (last path))]
               (case style
                 :mean
                 (println (str (format "%-32s" label) indent (fmt-dur mean)))

                 (let [tok (fn [k v] (format "%-13s" (str k " " (fmt-dur v))))]
                   (println (str (format "%-22s" label)
                                 (format "%-9s" (str "n " n))
                                 (tok "sum"  sum)
                                 (tok "mean" mean)
                                 (tok "min"  min)
                                 (tok "max"  max)))))))))))))

               
(do
  (println "COLUMNS env:" (System/getenv "COLUMNS"))
(println "stty:" (try (:out (clojure.java.shell/sh "bash" "-c" "stty size < /dev/tty"))
                      (catch Exception e (str "ERR " (.getMessage e)))))
(println "tput:" (try (:out (clojure.java.shell/sh "bash" "-c" "tput cols < /dev/tty"))
                      (catch Exception e (str "ERR " (.getMessage e)))))
(println "term-width:" (#'fireworks.prof/term-width))
  )
