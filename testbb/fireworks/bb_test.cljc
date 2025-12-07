(ns
 fireworks.bb-test
 (:require
  [clojure.string :as string]
  [fireworks.test-util :refer [escape-sgr]]
  [fireworks.core :refer [? !? ?> !?>]]
  [fireworks.sample :as sample]
  [clojure.test :refer [deftest is]]))


(do
 (deftest !?-par (is (= (!? "foo") "foo")))
 (deftest ?>-par (is (= (?> "foo") "foo")))
 (deftest !?>-par (is (= (!?> "foo") "foo"))))


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
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :format-label-as-code? false,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/custom-vector-datatype)
       :formatted
       :string-with-ansi-sgr-tags
       escape-sgr
       string/join)
      "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomVector〠0〠\n〠38;5;102;48;2;255;249;245〠[〠0〠〠38;2;77;109;186〠...〠0〠〠38;5;102;48;2;255;249;245〠]〠0〠"))))

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
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :format-label-as-code? false,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/custom-map-datatype)
       :formatted
       :string-with-ansi-sgr-tags
       escape-sgr
       string/join)
      "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomMap〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;77;109;186〠 ...〠0〠〠〠 〠0〠〠38;2;77;109;186〠〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠"))))

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
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :format-label-as-code? false,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/vector-with-custom-datatypes)
       :formatted
       :string-with-ansi-sgr-tags
       escape-sgr
       string/join)
      "〠38;5;102〠[〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomVector〠0〠\n 〠38;5;32;48;2;255;249;245〠[〠0〠〠38;2;77;109;186〠...〠0〠〠38;5;32;48;2;255;249;245〠]〠0〠〠〠\n 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.CustomMap〠0〠\n 〠38;5;32;48;2;255;249;245〠{〠0〠〠38;2;77;109;186〠 ...〠0〠〠〠 〠0〠〠38;2;77;109;186〠〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠38;5;102〠]〠0〠"))))

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
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :enable-terminal-truecolor? true,
         :non-coll-depth-1-length-limit 69,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :format-label-as-code? false,
         :non-coll-result-length-limit 444,
         :theme "Alabaster Light",
         :metadata-print-level 7,
         :coll-limit 40,
         :label-length-limit 44,
         :non-coll-length-limit 33,
         :single-line-coll-length-limit 33,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :legacy-terminal? false,
         :custom-printers {},
         :margin-inline-start 0,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :bracket-contrast "high"}
        sample/user-fn-names)
       :formatted
       :string-with-ansi-sgr-tags
       escape-sgr
       string/join)
      "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:user-fn〠0〠〠〠       〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/xy〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:really-long-fn〠0〠〠〠 〠0〠〠38;2;77;109;186〠xyasldfasldkfaslkjfzzzzzzzzzz〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;2;153;153;153〠[]〠0〠〠38;5;102〠}〠0〠"))))

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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 40,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/array-map-of-everything-cljc)
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:string〠0〠〠〠          〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"string\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:regex〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;68;140;39〠#\"myregex\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:uuid〠0〠〠〠            〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠#uuid 〠0〠〠38;2;68;140;39〠\"4fe5d828-6444-11e8-8222\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:symbol〠0〠〠〠          〠0〠〠〠 〠0〠〠38;2;77;109;186〠mysym〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:symbol+meta〠0〠〠〠     〠0〠〠〠 〠0〠〠38;2;77;109;186〠mysym〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:boolean〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;122;62;157〠true〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:keyword〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;122;62;157〠:keyword〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:nil〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠nil〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:##Nan〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;122;62;157〠NaN〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:##Inf〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;122;62;157〠Infinity〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:##-Inf〠0〠〠〠          〠0〠〠〠 〠0〠〠38;2;122;62;157〠-Infinity〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:int〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠1234〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:float〠0〠〠〠           〠0〠〠〠 〠0〠〠38;2;122;62;157〠3.33〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:lambda〠0〠〠〠          〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:lambda-2-args〠0〠〠〠   〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠λ〠0〠〠38;2;77;109;186〠〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:core-fn〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;77;109;186〠clojure.core/juxt〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:date-fn〠0〠〠〠         〠0〠〠〠 〠0〠〠38;2;77;109;186〠java.util/Date〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:datatype-class〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/MyType〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:recordtype-class〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/MyRecordType〠0〠〠38;2;153;153;153〠[]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:map〠0〠〠〠             〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠:c〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"three\"〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:multiline-map〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠    〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuv\"〠0〠〠〠\n                    〠0〠〠38;2;122;62;157〠:ab〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"abcdefghijklmnopqrstuv12345\"〠0〠〠〠\n                    〠0〠〠38;2;122;62;157〠:abcde〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"xyz\"〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:rainbow〠0〠〠〠         〠0〠〠〠 〠0〠〠38;5;32〠[〠0〠〠38;5;172〠[〠0〠〠38;5;71〠[〠0〠〠38;5;170〠[〠0〠〠38;5;102〠[〠0〠〠38;5;102〠]〠0〠〠38;5;170〠]〠0〠〠38;5;71〠]〠0〠〠38;5;172〠]〠0〠〠38;5;32〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:vector〠0〠〠〠          〠0〠〠〠 〠0〠〠38;5;32〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;32〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:vector+meta〠0〠〠〠     〠0〠〠〠 〠0〠〠38;5;32;48;2;252;240;255〠[〠0〠〠38;2;77;109;186〠foo〠0〠〠〠\n                    〠0〠〠38;2;77;109;186〠bar〠0〠〠〠\n                    〠0〠〠38;2;77;109;186〠baz〠0〠〠38;5;32;48;2;252;240;255〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:set〠0〠〠〠             〠0〠〠〠 〠0〠〠38;5;32〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"three\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:2〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:list〠0〠〠〠            〠0〠〠〠 〠0〠〠38;5;32〠(〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;32〠)〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:lazy-seq〠0〠〠〠        〠0〠〠〠 〠0〠〠38;5;32〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠38;5;32〠)〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:record〠0〠〠〠          〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n                   〠38;5;32;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:atom〠0〠〠〠            〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Atom<〠0〠〠38;2;122;62;157〠1〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠>〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:volatile!〠0〠〠〠       〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Volatile<〠0〠〠38;2;122;62;157〠1〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠>〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:transient-vector〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠TransientVector〠0〠\n                   〠38;5;32;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠38;5;32;48;2;255;249;245〠]〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:transient-set〠0〠〠〠   〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠TransientHashSet〠0〠\n                   〠38;5;32;48;2;255;249;245〠#{〠0〠〠3;38;2;140;140;140〠〠〠...+2〠0〠〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:transient-map〠0〠〠〠   〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠TransientArrayMap〠0〠\n                   〠38;5;32;48;2;255;249;245〠{〠0〠〠3;38;2;140;140;140〠 〠〠......+2〠0〠〠0〠〠38;5;32;48;2;255;249;245〠}〠0〠〠38;5;102〠}〠0〠")))


(deftest
 no-truncation
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 40,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? false,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (cons
      "adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx"
      (range 50)))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;68;140;39〠\"adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠0〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠2〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠4〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠5〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠6〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠7〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠8〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠9〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠10〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠11〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠12〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠13〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠14〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠15〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠16〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠17〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠18〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠19〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠20〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠21〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠22〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠23〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠24〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠25〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠26〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠27〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠28〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠29〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠30〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠31〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠32〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠33〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠34〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠35〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠36〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠37〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠38〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠39〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠40〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠41〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠42〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠43〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠44〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠45〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠46〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠47〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠48〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠49〠0〠〠38;5;102〠)〠0〠")))


(deftest
 bolded
 (is
  (=
   (->
    (?
     :data
     {:non-coll-mapkey-length-limit 20,
      :line-height 1.45,
      :find nil,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? true,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 40,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/array-map-of-everything-cljc)
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102;1〠{〠0〠〠38;2;122;62;157;1〠:string〠0〠〠1〠          〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"string\"〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:regex〠0〠〠1〠           〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠#\"myregex\"〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:uuid〠0〠〠1〠            〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠#uuid 〠0〠〠38;2;68;140;39;1〠\"4fe5d828-6444-11e8-8222\"〠3;38;2;140;140;140;1〠...〠0〠〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:symbol〠0〠〠1〠          〠0〠〠1〠 〠0〠〠38;2;77;109;186;1〠mysym〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:symbol+meta〠0〠〠1〠     〠0〠〠1〠 〠0〠〠38;2;77;109;186;1〠mysym〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:boolean〠0〠〠1〠         〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠true〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:keyword〠0〠〠1〠         〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠:keyword〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:nil〠0〠〠1〠             〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠nil〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:##Nan〠0〠〠1〠           〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠NaN〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:##Inf〠0〠〠1〠           〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠Infinity〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:##-Inf〠0〠〠1〠          〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠-Infinity〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:int〠0〠〠1〠             〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠1234〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:float〠0〠〠1〠           〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠3.33〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:lambda〠0〠〠1〠          〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠λ〠0〠〠38;2;77;109;186;1〠〠0〠〠38;2;153;153;153;1〠[]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:lambda-2-args〠0〠〠1〠   〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠λ〠0〠〠38;2;77;109;186;1〠〠0〠〠38;2;153;153;153;1〠[]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:core-fn〠0〠〠1〠         〠0〠〠1〠 〠0〠〠38;2;77;109;186;1〠clojure.core/juxt〠0〠〠38;2;153;153;153;1〠[]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:date-fn〠0〠〠1〠         〠0〠〠1〠 〠0〠〠38;2;77;109;186;1〠java.util/Date〠0〠〠38;2;153;153;153;1〠[]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:datatype-class〠0〠〠1〠  〠0〠〠1〠 〠0〠〠38;2;77;109;186;1〠fireworks.sample/MyType〠0〠〠38;2;153;153;153;1〠[]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:recordtype-class〠0〠〠1〠 〠0〠〠38;2;77;109;186;1〠fireworks.sample/MyRecordType〠0〠〠38;2;153;153;153;1〠[]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:map〠0〠〠1〠             〠0〠〠1〠 〠0〠〠38;5;32;1〠{〠0〠〠38;2;122;62;157;1〠:a〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠1〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠:b〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠2〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠:c〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"three\"〠0〠〠38;5;32;1〠}〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:multiline-map〠0〠〠1〠   〠0〠〠1〠 〠0〠〠38;5;32;1〠{〠0〠〠38;2;122;62;157;1〠:a〠0〠〠1〠    〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"abcdefghijklmnopqrstuv\"〠0〠〠1〠\n                    〠0〠〠38;2;122;62;157;1〠:ab〠0〠〠1〠   〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"abcdefghijklmnopqrstuv12345\"〠0〠〠1〠\n                    〠0〠〠38;2;122;62;157;1〠:abcde〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"xyz\"〠0〠〠38;5;32;1〠}〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:rainbow〠0〠〠1〠         〠0〠〠1〠 〠0〠〠38;5;32;1〠[〠0〠〠38;5;172;1〠[〠0〠〠38;5;71;1〠[〠0〠〠38;5;170;1〠[〠0〠〠38;5;102;1〠[〠0〠〠38;5;102;1〠]〠0〠〠38;5;170;1〠]〠0〠〠38;5;71;1〠]〠0〠〠38;5;172;1〠]〠0〠〠38;5;32;1〠]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:vector〠0〠〠1〠          〠0〠〠1〠 〠0〠〠38;5;32;1〠[〠0〠〠38;2;122;62;157;1〠1〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠2〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠3〠0〠〠38;5;32;1〠]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:vector+meta〠0〠〠1〠     〠0〠〠1〠 〠0〠〠38;5;32;1;48;2;252;240;255〠[〠0〠〠38;2;77;109;186;1〠foo〠0〠〠1〠\n                    〠0〠〠38;2;77;109;186;1〠bar〠0〠〠1〠\n                    〠0〠〠38;2;77;109;186;1〠baz〠0〠〠38;5;32;1;48;2;252;240;255〠]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:set〠0〠〠1〠             〠0〠〠1〠 〠0〠〠38;5;32;1〠#{〠0〠〠38;2;122;62;157;1〠1〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"three\"〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠:2〠0〠〠38;5;32;1〠}〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:list〠0〠〠1〠            〠0〠〠1〠 〠0〠〠38;5;32;1〠(〠0〠〠38;2;122;62;157;1〠1〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠2〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠3〠0〠〠38;5;32;1〠)〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:lazy-seq〠0〠〠1〠        〠0〠〠1〠 〠0〠〠38;5;32;1〠(〠0〠〠38;2;122;62;157;1〠0〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠1〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠2〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠3〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠4〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠5〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠6〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠7〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠8〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠9〠0〠〠38;5;32;1〠)〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:record〠0〠〠1〠          〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n                   〠38;5;32;1;48;2;255;249;245〠{〠0〠〠38;2;122;62;157;1〠:a〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"a\"〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠:b〠0〠〠1〠 〠0〠〠38;2;68;140;39;1〠\"b\"〠0〠〠38;5;32;1;48;2;255;249;245〠}〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:atom〠0〠〠1〠            〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠Atom<〠0〠〠38;2;122;62;157;1〠1〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠>〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:volatile!〠0〠〠1〠       〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠Volatile<〠0〠〠38;2;122;62;157;1〠1〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠>〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:transient-vector〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠TransientVector〠0〠\n                   〠38;5;32;1;48;2;255;249;245〠[〠0〠〠38;2;122;62;157;1〠1〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠2〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠3〠0〠〠1〠 〠0〠〠38;2;122;62;157;1〠4〠0〠〠38;5;32;1;48;2;255;249;245〠]〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:transient-set〠0〠〠1〠   〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠TransientHashSet〠0〠\n                   〠38;5;32;1;48;2;255;249;245〠#{〠0〠〠3;38;2;140;140;140;1〠〠1〠...+2〠0〠〠0〠〠38;5;32;1;48;2;255;249;245〠}〠0〠〠1〠\n 〠0〠〠38;2;122;62;157;1〠:transient-map〠0〠〠1〠   〠0〠〠1〠 〠0〠〠3;38;2;199;104;35;1;48;2;255;249;245〠TransientArrayMap〠0〠\n                   〠38;5;32;1;48;2;255;249;245〠{〠0〠〠3;38;2;140;140;140;1〠 〠1〠......+2〠0〠〠0〠〠38;5;32;1;48;2;255;249;245〠}〠0〠〠38;5;102;1〠}〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 50,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (range 14))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠〠 〠0〠〠38;2;122;62;157〠11〠0〠〠〠 〠0〠〠38;2;122;62;157〠12〠0〠〠〠 〠0〠〠38;2;122;62;157〠13〠0〠〠38;5;102〠)〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 50,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (range 15))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠〠 〠0〠〠38;2;122;62;157〠11〠0〠〠〠 〠0〠〠38;2;122;62;157〠12〠0〠〠〠 〠0〠〠38;2;122;62;157〠13〠0〠〠〠 〠0〠〠38;2;122;62;157〠14〠0〠〠38;5;102〠)〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9 :j 10))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:c〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:d〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:e〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:f〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:g〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:h〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:i〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:j〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠38;5;102〠}〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (atom sample/my-record-type))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠Atom<〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠>〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/my-record-type)
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (with-meta 'mysym {:foo :bar}))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;2;77;109;186〠mysym〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     [[[[[]]]]])
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠[〠0〠〠38;5;32〠[〠0〠〠38;5;172〠[〠0〠〠38;5;71〠[〠0〠〠38;5;170〠[〠0〠〠38;5;170〠]〠0〠〠38;5;71〠]〠0〠〠38;5;172〠]〠0〠〠38;5;32〠]〠0〠〠38;5;102〠]〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "low"}
     [[[[[]]]]])
    :formatted
    :string-with-ansi-sgr-tags
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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 60,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠[〠0〠〠38;2;68;140;39〠\"asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44\"〠3;38;2;140;140;140〠...〠0〠〠0〠〠38;5;102〠]〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 44,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
    :formatted
    :string-with-ansi-sgr-tags
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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 100,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     sample/interop-types)
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;68;140;39〠\"Java collection types\"〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;77;109;186〠java.util.ArrayList〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.ArrayList〠0〠\n                                              〠38;5;172;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠38;5;172;48;2;255;249;245〠]〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.util.HashMap〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n                                              〠38;5;172;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;172;48;2;255;249;245〠}〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.util.HashSet〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n                                              〠38;5;172;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;172;48;2;255;249;245〠}〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.lang.String〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"welcome\"〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠array〠0〠〠〠              〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Ljava.lang.Object〠0〠\n                                              〠38;5;172;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠38;5;172;48;2;255;249;245〠]〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;68;140;39〠\"Java numbers\"〠0〠〠〠         〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:ratio〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠1/3〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:byte〠0〠〠〠               〠0〠〠〠 〠0〠〠38;2;122;62;157〠0〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:short〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:double〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠23.44〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:decimal〠0〠〠〠            〠0〠〠〠 〠0〠〠38;2;122;62;157〠1M〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:int〠0〠〠〠                〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:float〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠1.5〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:char〠0〠〠〠               〠0〠〠〠 〠0〠〠〠a〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:java.math.BigInt〠3;38;2;140;140;140〠...〠0〠〠0〠〠〠 〠0〠〠38;2;122;62;157〠171〠0〠〠38;5;32〠}〠0〠〠38;5;102〠}〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (java.util.HashMap. {"a" 1, "b" 2}))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (java.util.ArrayList. [1 2 3]))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.ArrayList〠0〠\n〠38;5;102;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;102;48;2;255;249;245〠]〠0〠")))


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
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :enable-terminal-truecolor? true,
      :non-coll-depth-1-length-limit 69,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :format-label-as-code? false,
      :non-coll-result-length-limit 444,
      :theme "Alabaster Light",
      :metadata-print-level 7,
      :coll-limit 33,
      :label-length-limit 44,
      :non-coll-length-limit 33,
      :single-line-coll-length-limit 33,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :legacy-terminal? false,
      :custom-printers {},
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :bracket-contrast "high"}
     (java.util.HashSet. #{1 "a" 2 "b"}))
    :formatted
    :string-with-ansi-sgr-tags
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n〠38;5;102;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"a\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"b\"〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))
