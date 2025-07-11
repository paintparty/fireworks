(ns
 fireworks.test-suite
 (:require
  [clojure.string :as string]
  [fireworks.test-util :refer [escape-sgr]]
  [fireworks.core :refer [? !? ?> !?>]]
  [fireworks.config]
  [fireworks.demo]
  [fireworks.sample :as sample]
  [fireworks.smoke-test]
  [clojure.test :refer [deftest is]]))


(deftest !?-par
                (is (= (!? "foo") "foo")))
              (deftest ?>-par
                (is (= (?> "foo") "foo")))
              (deftest !?>-par
                (is (= (!?> "foo") "foo")))
#?(:bb nil
   :clj
   (deftest
    custom-vector-datatype
    (is
     (=
      (->
       (?
        :data
        {:non-coll-mapkey-length-limit 20,
         :line-height 1.45,
         :find nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :margin-top 0,
         :print-level 7,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? true,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/custom-vector-datatype)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomVector〠0〠\n〠38;5;241;48;2;255;249;245〠[〠0〠〠38;2;77;109;186〠...〠0〠〠38;5;241;48;2;255;249;245〠]〠0〠"))))

#?(:bb nil
   :clj
   (deftest
    custom-map-dataype
    (is
     (=
      (->
       (?
        :data
        {:non-coll-mapkey-length-limit 20,
         :line-height 1.45,
         :find nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :margin-top 0,
         :print-level 7,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? true,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/custom-map-datatype)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomMap〠0〠\n〠38;5;241;48;2;255;249;245〠{〠0〠〠38;2;77;109;186〠 ...〠0〠〠〠 〠0〠〠38;2;77;109;186〠〠0〠〠38;5;241;48;2;255;249;245〠}〠0〠"))))

#?(:bb nil
   :clj
   (deftest
    custom-map-dataype
    (is
     (=
      (->
       (?
        :data
        {:non-coll-mapkey-length-limit 20,
         :line-height 1.45,
         :find nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :margin-top 0,
         :print-level 7,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? true,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/vector-with-custom-datatypes)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠38;5;241〠[〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomVector〠0〠\n 〠38;5;32;48;2;255;249;245〠[〠0〠〠38;2;77;109;186〠...〠0〠〠38;5;32;48;2;255;249;245〠]〠0〠〠〠\n 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomMap〠0〠\n 〠38;5;32;48;2;255;249;245〠{〠0〠〠38;2;77;109;186〠 ...〠0〠〠〠 〠0〠〠38;2;77;109;186〠〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠38;5;241〠]〠0〠"))))

#?(:bb nil
   :clj
   (deftest
    user-fn-names
    (is
     (=
      (->
       (?
        :data
        {:non-coll-mapkey-length-limit 20,
         :line-height 1.45,
         :find nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :margin-top 0,
         :print-level 7,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? true,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/user-fn-names)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠38;5;241〠{〠0〠〠38;2;122;62;157〠:user-fn〠0〠〠〠       〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/xy〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:really-long-fn〠0〠〠〠 〠0〠〠38;2;77;109;186〠xyasldfasldkfaslkjfzzzzzzzzzz〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;2;153;153;153〠[]〠0〠〠38;5;241〠}〠0〠"))))

(deftest
 basic-samples
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 40,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/array-map-of-everything-cljc)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠{〠0〠〠38;2;122;62;157〠:string〠0〠〠〠          〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"string\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:regex〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;68;140;39〠#\"myregex\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:uuid〠0〠〠〠            〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠#uuid 〠0〠〠38;2;68;140;39〠\"4fe5d828-6444-11e8-8222\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:symbol〠0〠〠〠          〠0〠〠〠 〠0〠〠38;2;77;109;186〠mysym〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:symbol+meta〠0〠〠〠     〠0〠〠〠 〠0〠〠38;2;77;109;186〠mysym〠0〠 〠38;2;190;85;187;48;2;252;240;255〠    〠0〠〠38;2;190;85;187;48;2;252;240;255〠^{〠0〠〠38;2;190;85;187;48;2;252;240;255〠:foo〠0〠〠38;2;190;85;187;48;2;252;240;255〠 〠0〠〠38;2;190;85;187;48;2;252;240;255〠\"bar\"〠0〠〠38;2;190;85;187;48;2;252;240;255〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:boolean〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;122;62;157〠true〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:keyword〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;122;62;157〠:keyword〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:nil〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠nil〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:##Nan〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;122;62;157〠NaN〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:##Inf〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;122;62;157〠Infinity〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:##-Inf〠0〠〠〠          〠0〠〠〠 〠0〠〠38;2;122;62;157〠-Infinity〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:int〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠1234〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:float〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;122;62;157〠3.33〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:lambda〠0〠〠〠          〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:lambda-2-args〠0〠〠〠   〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:core-fn〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;77;109;186〠clojure.core/juxt〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:date-fn〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;77;109;186〠java.util/Date〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:datatype-class〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/MyType〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:recordtype-class〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/MyRecordType〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:map〠0〠〠〠             〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠:c〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"three\"〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:multiline-map〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠    〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuv\"〠0〠〠〠\n                    〠0〠〠38;2;122;62;157〠:ab〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuv12345\"〠0〠〠〠\n                    〠0〠〠38;2;122;62;157〠:abcde〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"xyz\"〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:rainbow〠0〠〠〠         〠0〠〠〠 〠0〠〠38;5;32〠[〠0〠〠38;5;208〠[〠0〠〠38;5;28〠[〠0〠〠38;5;128〠[〠0〠〠38;5;241〠[〠0〠〠38;5;241〠]〠0〠〠38;5;128〠]〠0〠〠38;5;28〠]〠0〠〠38;5;208〠]〠0〠〠38;5;32〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:vector〠0〠〠〠          〠0〠〠〠 〠0〠〠38;5;32〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;32〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:vector+meta〠0〠〠〠     〠0〠〠〠 〠0〠〠38;5;32;48;2;252;240;255〠[〠0〠 〠38;2;190;85;187;48;2;252;240;255〠    〠0〠〠38;2;190;85;187;48;2;252;240;255〠^{〠0〠〠38;2;190;85;187;48;2;252;240;255〠:meta-on-coll〠0〠〠38;2;190;85;187;48;2;252;240;255〠 〠0〠〠38;2;190;85;187;48;2;252;240;255〠\"abc\"〠0〠〠38;2;190;85;187;48;2;252;240;255〠}〠0〠〠〠\n                    〠0〠〠38;2;77;109;186〠foo〠0〠〠〠\n                    〠0〠〠38;2;77;109;186〠bar〠0〠 〠38;2;190;85;187;48;2;252;240;255〠    〠0〠〠38;2;190;85;187;48;2;252;240;255〠^{〠0〠〠38;2;190;85;187;48;2;252;240;255〠:meta-on-sym〠0〠〠38;2;190;85;187;48;2;252;240;255〠 〠0〠〠38;2;190;85;187;48;2;252;240;255〠\"xyz\"〠0〠〠38;2;190;85;187;48;2;252;240;255〠}〠0〠〠〠\n                    〠0〠〠38;2;77;109;186〠baz〠0〠〠38;5;32;48;2;252;240;255〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:set〠0〠〠〠             〠0〠〠〠 〠0〠〠38;5;32〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"three\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:2〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:list〠0〠〠〠            〠0〠〠〠 〠0〠〠38;5;32〠(〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;32〠)〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:lazy-seq〠0〠〠〠        〠0〠〠〠 〠0〠〠38;5;32〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠38;5;32〠)〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:record〠0〠〠〠          〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n                   〠38;5;32;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:atom〠0〠〠〠            〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Atom<〠0〠〠38;2;122;62;157〠1〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠>〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:volatile!〠0〠〠〠       〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Volatile<〠0〠〠38;2;122;62;157〠1〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠>〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:transient-vector〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠TransientVector〠0〠\n                   〠38;5;32;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠38;5;32;48;2;255;249;245〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:transient-set〠0〠〠〠   〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠TransientHashSet〠0〠\n                   〠38;5;32;48;2;255;249;245〠#{〠0〠〠3;38;2;140;140;140〠〠〠...+2〠0〠〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:transient-map〠0〠〠〠   〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠TransientArrayMap〠0〠\n                   〠38;5;32;48;2;255;249;245〠{〠0〠〠3;38;2;140;140;140〠 〠〠......+2〠0〠〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠38;5;241〠}〠0〠")))


(deftest
 with-coll-limit
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 5,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     [1 2 3 4 5 6 7 8])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠3;38;2;140;140;140〠 〠〠...+3〠0〠〠0〠〠38;5;241〠]〠0〠")))


(deftest
 single-line-coll-length-limit-50-19
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 50,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (range 14))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠〠 〠0〠〠38;2;122;62;157〠11〠0〠〠〠 〠0〠〠38;2;122;62;157〠12〠0〠〠〠 〠0〠〠38;2;122;62;157〠13〠0〠〠38;5;241〠)〠0〠")))


(deftest
 single-line-coll-length-limit-50-20
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 50,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (range 15))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠〠 〠0〠〠38;2;122;62;157〠11〠0〠〠〠 〠0〠〠38;2;122;62;157〠12〠0〠〠〠 〠0〠〠38;2;122;62;157〠13〠0〠〠〠 〠0〠〠38;2;122;62;157〠14〠0〠〠38;5;241〠)〠0〠")))


(deftest
 array-map-order
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9 :j 10))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:c〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:d〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:e〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:f〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:g〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:h〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:i〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:j〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠38;5;241〠}〠0〠")))


(deftest
 record-sample-in-atom
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (atom sample/my-record-type))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠Atom<〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n〠38;5;241;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;241;48;2;255;249;245〠}〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠>〠0〠")))


(deftest
 record-sample
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/my-record-type)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n〠38;5;241;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;241;48;2;255;249;245〠}〠0〠")))


(deftest
 symbol-with-meta
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (with-meta 'mysym {:foo :bar}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;77;109;186〠mysym〠0〠 〠38;2;190;85;187;48;2;252;240;255〠    〠0〠〠38;2;190;85;187;48;2;252;240;255〠^{〠0〠〠38;2;190;85;187;48;2;252;240;255〠:foo〠0〠〠38;2;190;85;187;48;2;252;240;255〠 〠0〠〠38;2;190;85;187;48;2;252;240;255〠:bar〠0〠〠38;2;190;85;187;48;2;252;240;255〠}〠0〠")))


(deftest
 rainbow-brackets
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     [[[[[]]]]])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠[〠0〠〠38;5;32〠[〠0〠〠38;5;208〠[〠0〠〠38;5;28〠[〠0〠〠38;5;128〠[〠0〠〠38;5;128〠]〠0〠〠38;5;28〠]〠0〠〠38;5;208〠]〠0〠〠38;5;32〠]〠0〠〠38;5;241〠]〠0〠")))


(deftest
 rainbow-brackets-low-contrast
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "low"}
     [[[[[]]]]])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;245〠[〠0〠〠38;5;74〠[〠0〠〠38;5;179〠[〠0〠〠38;5;106〠[〠0〠〠38;5;177〠[〠0〠〠38;5;177〠]〠0〠〠38;5;106〠]〠0〠〠38;5;179〠]〠0〠〠38;5;74〠]〠0〠〠38;5;245〠]〠0〠")))


(deftest
 with-non-coll-level-1-depth-length-limit
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 60,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠[〠0〠〠38;2;68;140;39〠\"asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;5;241〠]〠0〠")))


(deftest
 non-coll-result-length-limit
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 44,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;68;140;39〠\"asdfffaaaaasdfasdfasdfasdfasdfasdfasdfa\"...〠0〠")))


(deftest
 java-interop-types
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 100,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/interop-types)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;241〠{〠0〠〠38;2;68;140;39〠\"Java collection types\"〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;77;109;186〠java.util.ArrayList〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.ArrayList〠0〠\n                                              〠38;5;208;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠38;5;208;48;2;255;249;245〠]〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.util.HashMap〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n                                              〠38;5;208;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;208;48;2;255;249;245〠}〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.util.HashSet〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n                                              〠38;5;208;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;208;48;2;255;249;245〠}〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.lang.String〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"welcome\"〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠array〠0〠〠〠              〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Ljava.lang.Object〠0〠\n                                              〠38;5;208;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠38;5;208;48;2;255;249;245〠]〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;68;140;39〠\"Java numbers\"〠0〠〠〠         〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:ratio〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠1/3〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:byte〠0〠〠〠               〠0〠〠〠 〠0〠〠38;2;122;62;157〠0〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:short〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:double〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠23.44〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:decimal〠0〠〠〠            〠0〠〠〠 〠0〠〠38;2;122;62;157〠1M〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:int〠0〠〠〠                〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:float〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠1.5〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:char〠0〠〠〠               〠0〠〠〠 〠0〠〠〠a〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:java.math.BigInt〠3;38;2;140;140;140〠...〠0〠〠0〠〠〠 〠0〠〠38;2;122;62;157〠171〠0〠〠38;5;32〠}〠0〠〠38;5;241〠}〠0〠")))


(deftest
 java-util-hashmap
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (java.util.HashMap. {"a" 1, "b" 2}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n〠38;5;241;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;241;48;2;255;249;245〠}〠0〠")))


(deftest
 java-util-arraylist
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (java.util.ArrayList. [1 2 3]))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.ArrayList〠0〠\n〠38;5;241;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;241;48;2;255;249;245〠]〠0〠")))


(deftest
 java-util-hashset
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :margin-top 0,
      :print-level 7,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (java.util.HashSet. #{1 "a" 2 "b"}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n〠38;5;241;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;241;48;2;255;249;245〠}〠0〠")))
