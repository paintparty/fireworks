;; This namespace is automatically generated in `fireworks.core-test`.

;; Do not manually add anything to this namespace.

;; To regenerate, follow the instructions in the docstring of `fireworks.core-test/write-tests-ns!`
;; If you want do any experimentation use `fireworks.smoke-test`


(ns
 fireworks.test-suite
 (:require
  [clojure.string :as string]
  [fireworks.test-util :refer [escape-sgr visual-mode?]]
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
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :print-with nil,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :select-metadata-keys nil,
         :enable-terminal-truecolor? true,
         :quote-lists? false,
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Light",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :label-color nil,
         :scalar-mapkey-max-length 33,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :print-level-inline-results 3,
         :legacy-terminal? false,
         :quote-symbols? false,
         :colorize? true,
         :label-max-length 44,
         :margin-inline-start 0,
         :print-length-inline-results 8,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        sample/custom-vector-datatype)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠38;5;102〠[〠0〠〠38;2;77;109;186〠...〠0〠〠38;5;102〠]〠0〠"))))

#?(:bb nil
   :clj
   (deftest
    custom-map-datatype
    (is
     (=
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :print-with nil,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :select-metadata-keys nil,
         :enable-terminal-truecolor? true,
         :quote-lists? false,
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Light",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :label-color nil,
         :scalar-mapkey-max-length 33,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :print-level-inline-results 3,
         :legacy-terminal? false,
         :quote-symbols? false,
         :colorize? true,
         :label-max-length 44,
         :margin-inline-start 0,
         :print-length-inline-results 8,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        sample/custom-map-datatype)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠38;5;102〠{〠0〠〠38;2;77;109;186〠 ...〠0〠〠〠 〠0〠〠38;2;77;109;186〠〠0〠〠38;5;102〠}〠0〠"))))

#?(:bb nil
   :clj
   (deftest
    vector-with-custom-datatype
    (is
     (=
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :print-with nil,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :select-metadata-keys nil,
         :enable-terminal-truecolor? true,
         :quote-lists? false,
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Light",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :label-color nil,
         :scalar-mapkey-max-length 33,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :print-level-inline-results 3,
         :legacy-terminal? false,
         :quote-symbols? false,
         :colorize? true,
         :label-max-length 44,
         :margin-inline-start 0,
         :print-length-inline-results 8,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        sample/vector-with-custom-datatypes)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠38;5;102〠[〠0〠〠38;5;32〠[〠0〠〠38;2;77;109;186〠...〠0〠〠38;5;32〠]〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;77;109;186〠 ...〠0〠〠〠 〠0〠〠38;2;77;109;186〠〠0〠〠38;5;32〠}〠0〠〠38;5;102〠]〠0〠"))))

#?(:bb nil
   :clj
   (deftest
    user-fn-names
    (is
     (=
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level nil,
         :enable-terminal-italics? true,
         :print-with nil,
         :display-namespaces? true,
         :enable-rainbow-brackets? true,
         :select-metadata-keys nil,
         :enable-terminal-truecolor? true,
         :quote-lists? false,
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Light",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :label-color nil,
         :scalar-mapkey-max-length 33,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :print-level-inline-results 3,
         :legacy-terminal? false,
         :quote-symbols? false,
         :colorize? true,
         :label-max-length 44,
         :margin-inline-start 0,
         :print-length-inline-results 8,
         :display-metadata? false,
         :margin-bottom 1,
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        sample/user-fn-names)
       :formatted
       :string
       escape-sgr
       string/join)
      "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:user-fn〠0〠〠〠       〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/xy〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:really-long-fn〠0〠〠〠 〠0〠〠38;2;77;109;186〠fireworks.sample/xyasldfasldkf〠0〠〠3;38;2;140;140;140〠...〠0〠〠38;5;102〠}〠0〠"))))

(deftest
 no-truncation
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 40,
      :multi-line-metadata? true,
      :truncate? false,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (cons
      "adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx"
      (range 50)))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx〠0〠〠38;2;68;140;39〠\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠0〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠2〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠4〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠5〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠6〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠7〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠8〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠9〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠10〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠11〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠12〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠13〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠14〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠15〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠16〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠17〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠18〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠19〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠20〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠21〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠22〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠23〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠24〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠25〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠26〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠27〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠28〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠29〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠30〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠31〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠32〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠33〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠34〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠35〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠36〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠37〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠38〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠39〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠40〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠41〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠42〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠43〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠44〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠45〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠46〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠47〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠48〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠49〠0〠〠38;5;102〠)〠0〠")))


(deftest
 string-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     "string")
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠string〠0〠〠38;2;68;140;39〠\"〠0〠")))


(deftest
 regex-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 100,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     #"^(?:abc\\\(\[\d)+[^a-z0-9\w]*$|^foobar{1}s?$")
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠〠0〠〠38;2;68;140;39〠〠〠#〠0〠〠〠\"〠0〠〠〠^〠0〠〠48;2;235;235;235〠(〠0〠〠48;2;235;235;235〠?〠0〠〠48;2;235;235;235〠:〠0〠〠48;2;235;235;235〠a〠0〠〠48;2;235;235;235〠b〠0〠〠48;2;235;235;235〠c〠0〠〠38;2;178;178;178;48;2;235;235;235〠\\〠0〠〠48;2;235;235;235〠\\〠0〠〠38;2;178;178;178;48;2;235;235;235〠\\〠0〠〠48;2;235;235;235〠(〠0〠〠48;2;235;235;235〠〠0〠〠38;2;178;178;178;48;2;235;235;235〠\\〠0〠〠48;2;235;235;235〠[〠0〠〠48;2;235;235;235〠〠0〠〠48;2;235;235;235〠\\d〠0〠〠48;2;235;235;235〠)〠0〠〠〠+〠0〠〠48;2;235;235;235〠[〠0〠〠48;2;235;235;235〠^〠0〠〠48;2;235;235;235〠a〠0〠〠48;2;235;235;235〠-〠0〠〠48;2;235;235;235〠z〠0〠〠48;2;235;235;235〠0〠0〠〠48;2;235;235;235〠-〠0〠〠48;2;235;235;235〠9〠0〠〠48;2;235;235;235〠\\w〠0〠〠48;2;235;235;235〠]〠0〠〠〠*〠0〠〠〠$〠0〠〠〠|〠0〠〠〠^〠0〠〠〠f〠0〠〠〠o〠0〠〠〠o〠0〠〠〠b〠0〠〠〠a〠0〠〠〠r〠0〠〠〠{〠0〠〠〠1〠0〠〠〠}〠0〠〠〠s〠0〠〠〠?〠0〠〠〠$〠0〠〠〠\"〠0〠〠0〠")))


(deftest
 uuid-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 100,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     #uuid "4fe5d828-6444-11e8-8222-720007e40350")
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠#uuid 〠0〠〠38;2;68;140;39〠\"4fe5d828-6444-11e8-8222-720007e40350\"〠0〠")))


(deftest
 symbol-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (symbol "mysym"))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;77;109;186〠mysym〠0〠")))


(deftest
 symbol+meta-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (with-meta (symbol "mysym") {:foo "bar"}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;77;109;186〠mysym〠0〠 〠38;2;190;85;187;48;2;252;240;255〠    〠0〠〠38;2;190;85;187;48;2;252;240;255〠^{〠0〠〠38;2;190;85;187;48;2;252;240;255〠:foo〠0〠〠38;2;190;85;187;48;2;252;240;255〠 〠0〠〠38;2;190;85;187;48;2;252;240;255〠\"〠0〠〠38;2;190;85;187;48;2;252;240;255〠bar〠0〠〠38;2;190;85;187;48;2;252;240;255〠\"〠0〠〠38;2;190;85;187;48;2;252;240;255〠}〠0〠")))


(deftest
 boolean-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     true)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠true〠0〠")))


(deftest
 keyword-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     :keyword)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠:keyword〠0〠")))


(deftest
 nil-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     nil)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠nil〠0〠")))


(deftest
 Nan-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     ##NaN)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠NaN〠0〠")))


(deftest
 Inf-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     ##Inf)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠Infinity〠0〠")))


(deftest
 -Inf-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     ##-Inf)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠-Infinity〠0〠")))


(deftest
 int-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     1234)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠1234〠0〠")))


(deftest
 float-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     3.33)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;122;62;157〠〠38;2;122;62;157〠3.〠0〠〠38;2;122;62;157〠33〠0〠〠0〠")))


(deftest
 core-fn
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     juxt)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;77;109;186〠clojure.core/juxt〠0〠")))


(deftest
 date-fn
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     java.util.Date)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;77;109;186〠java.util.Date〠0〠")))


(deftest
 map-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     {:a 1, :b 2, :c "three"})
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠:c〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠three〠0〠〠38;2;68;140;39〠\"〠0〠〠38;5;102〠}〠0〠")))


(deftest
 multiline-map
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     {:a "abcdefghijklmnopqrstuv",
      :ab "abcdefghijklmnopqrstuv12345",
      :abcde "xyz"})
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠    〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠abcdefghijklmnopqrstuv〠0〠〠38;2;68;140;39〠\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:ab〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠abcdefghijklmnopqrstuv12345〠0〠〠38;2;68;140;39〠\"〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:abcde〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠xyz〠0〠〠38;2;68;140;39〠\"〠0〠〠38;5;102〠}〠0〠")))


(deftest
 rainbow-brackets
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     [[[[[]]]]])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠[〠0〠〠38;5;32〠[〠0〠〠38;5;172〠[〠0〠〠38;5;71〠[〠0〠〠38;5;170〠[〠0〠〠38;5;170〠]〠0〠〠38;5;71〠]〠0〠〠38;5;172〠]〠0〠〠38;5;32〠]〠0〠〠38;5;102〠]〠0〠")))


(deftest
 vector-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     [1 2 3])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;102〠]〠0〠")))


(deftest
 vector+meta-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? true,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (with-meta [:foo :baz] {:meta-on-coll 1}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102;48;2;252;240;255〠[〠0〠 〠38;2;190;85;187;48;2;252;240;255〠    〠0〠〠38;2;190;85;187;48;2;252;240;255〠^{〠0〠〠38;2;190;85;187;48;2;252;240;255〠:meta-on-coll〠0〠〠38;2;190;85;187;48;2;252;240;255〠 〠0〠〠38;2;190;85;187;48;2;252;240;255〠1〠0〠〠38;2;190;85;187;48;2;252;240;255〠}〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:foo〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:baz〠0〠〠38;5;102;48;2;252;240;255〠]〠0〠")))


(deftest
 set-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     #{1 "three" :2})
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠three〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:2〠0〠〠38;5;102〠}〠0〠")))


(deftest
 list-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (list 1 2 3))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠38;5;102〠)〠0〠")))


(deftest
 lazy-seq-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (range 10))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠38;5;102〠)〠0〠")))


(deftest
 atom-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (atom
      {:orange 172,
       :gray 247,
       :white 231,
       :yellow 178,
       :green 76,
       :red 196,
       :blue 75,
       :magenta 171,
       :purple 141,
       :olive 106,
       :black 16}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠Atom〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:status〠0〠〠〠 〠0〠〠38;2;122;62;157〠:ready〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:val〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:orange〠0〠〠〠 〠0〠〠〠 〠0〠〠38;2;122;62;157〠172〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:gray〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;122;62;157〠247〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:white〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;122;62;157〠231〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:yellow〠0〠〠〠 〠0〠〠〠 〠0〠〠38;2;122;62;157〠178〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:green〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;122;62;157〠76〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:red〠0〠〠〠    〠0〠〠〠 〠0〠〠38;2;122;62;157〠196〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:blue〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;122;62;157〠75〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:magenta〠0〠〠〠 〠0〠〠38;2;122;62;157〠171〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:purple〠0〠〠〠 〠0〠〠〠 〠0〠〠38;2;122;62;157〠141〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:olive〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;122;62;157〠106〠0〠〠〠\n          〠0〠〠38;2;122;62;157〠:black〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;122;62;157〠16〠0〠〠38;5;32〠}〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 volatile!-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (volatile! 1))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠Volatile〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:status〠0〠〠〠 〠0〠〠38;2;122;62;157〠:ready〠0〠〠〠 〠0〠〠38;2;122;62;157〠:val〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 volatile
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (volatile! 1))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠Volatile〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:status〠0〠〠〠 〠0〠〠38;2;122;62;157〠:ready〠0〠〠〠 〠0〠〠38;2;122;62;157〠:val〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 transient-vector2
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (transient [1 2 3 4]))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠TransientVector〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠3;38;2;140;140;140〠〠〠...+4〠0〠〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 transient-set2
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (transient #{1 :a}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠TransientHashSet〠0〠〠〠#{}〠0〠")))


(deftest
 transient-map2
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (transient {1 2, 3 4}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠TransientArrayMap〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠3;38;2;140;140;140〠 〠〠...+2〠0〠〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 single-line-coll-print-length-50-19
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 50,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (range 14))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠(〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠〠 〠0〠〠38;2;122;62;157〠11〠0〠〠〠 〠0〠〠38;2;122;62;157〠12〠0〠〠〠 〠0〠〠38;2;122;62;157〠13〠0〠〠38;5;102〠)〠0〠")))


(deftest
 single-line-coll-print-length-50-20
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 50,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (range 15))
    :formatted
    :string
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
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9 :j 10))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:c〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:d〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:e〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:f〠0〠〠〠 〠0〠〠38;2;122;62;157〠6〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:g〠0〠〠〠 〠0〠〠38;2;122;62;157〠7〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:h〠0〠〠〠 〠0〠〠38;2;122;62;157〠8〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:i〠0〠〠〠 〠0〠〠38;2;122;62;157〠9〠0〠〠〠\n 〠0〠〠38;2;122;62;157〠:j〠0〠〠〠 〠0〠〠38;2;122;62;157〠10〠0〠〠38;5;102〠}〠0〠")))


(deftest
 symbol-with-meta
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (with-meta 'mysym {:foo :bar}))
    :formatted
    :string
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
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     [[[[[]]]]])
    :formatted
    :string
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
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "low"}
     [[[[[]]]]])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;245〠[〠0〠〠38;5;74〠[〠0〠〠38;5;179〠[〠0〠〠38;5;106〠[〠0〠〠38;5;177〠[〠0〠〠38;5;177〠]〠0〠〠38;5;106〠]〠0〠〠38;5;179〠]〠0〠〠38;5;74〠]〠0〠〠38;5;245〠]〠0〠")))


(deftest
 with-scalar-level-1-depth-print-length
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 60,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠[〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44〠0〠〠3;38;2;140;140;140〠...〠0〠〠38;2;68;140;39〠\"〠0〠〠38;5;102〠]〠0〠")))


(deftest
 scalar-result-max-length
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 44,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠asdfffaaaaasdfasdfasdfasdfasdfasdfasdfa〠0〠...〠38;2;68;140;39〠\"〠0〠")))


(deftest
 datatype-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     fireworks.sample/my-data-type)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyType〠0〠〠38;2;77;109;186〠fireworks.sample.MyType@6f45a2fd〠0〠")))


(deftest
 record-value
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     fireworks.sample/my-record-type)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠fireworks.sample.MyRecordType〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;122;62;157〠:a〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠a〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠:b〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠bbbbbbbbbbbbbb〠0〠〠38;2;68;140;39〠\"〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 java-interop-types
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 100,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     sample/interop-types)
    :formatted
    :string
    escape-sgr
    string/join)
   "〠38;5;102〠{〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠Java collection types〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;77;109;186〠java.util.ArrayList〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.ArrayList〠0〠\n                                              〠38;5;172;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠0〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠38;5;172;48;2;255;249;245〠]〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.util.HashMap〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n                                              〠38;5;172;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠a〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠b〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;172;48;2;255;249;245〠}〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.util.HashSet〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n                                              〠38;5;172;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠a〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠b〠0〠〠38;2;68;140;39〠\"〠0〠〠38;5;172;48;2;255;249;245〠}〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠java.lang.String〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠welcome〠0〠〠38;2;68;140;39〠\"〠0〠〠〠\n                          〠0〠〠38;2;77;109;186〠array〠0〠〠〠              〠0〠〠〠 〠0〠〠3;38;2;199;104;35;48;2;255;249;245〠Ljava.lang.Object〠0〠\n                                              〠38;5;172;48;2;255;249;245〠[〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠 〠0〠〠38;2;122;62;157〠4〠0〠〠〠 〠0〠〠38;2;122;62;157〠5〠0〠〠38;5;172;48;2;255;249;245〠]〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠Java numbers〠0〠〠38;2;68;140;39〠\"〠0〠〠〠         〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;2;122;62;157〠:ratio〠0〠〠〠               〠0〠〠〠 〠0〠〠38;2;122;62;157〠1/3〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:byte〠0〠〠〠                〠0〠〠〠 〠0〠〠38;2;122;62;157〠0〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:short〠0〠〠〠               〠0〠〠〠 〠0〠〠38;2;122;62;157〠3〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:double〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;122;62;157〠〠38;2;122;62;157〠23.〠0〠〠38;2;122;62;157〠44〠0〠〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:decimal〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;122;62;157〠1M〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:int〠0〠〠〠                 〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:float〠0〠〠〠               〠0〠〠〠 〠0〠〠38;2;122;62;157〠〠38;2;122;62;157〠1.〠0〠〠38;2;122;62;157〠5〠0〠〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:char〠0〠〠〠                〠0〠〠〠 〠0〠〠〠a〠0〠〠〠\n                          〠0〠〠38;2;122;62;157〠:java.math.BigInteger〠0〠〠〠 〠0〠〠38;2;122;62;157〠171〠0〠〠38;5;32〠}〠0〠〠38;5;102〠}〠0〠")))


(deftest
 java-util-hashmap
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (java.util.HashMap. {"a" 1, "b" 2}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashMap〠0〠\n〠38;5;102;48;2;255;249;245〠{〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠a〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠b〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))


(deftest
 java-util-arraylist
 (is
  (=
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (java.util.ArrayList. [1 2 3]))
    :formatted
    :string
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
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level nil,
      :enable-terminal-italics? true,
      :print-with nil,
      :display-namespaces? true,
      :enable-rainbow-brackets? true,
      :select-metadata-keys nil,
      :enable-terminal-truecolor? true,
      :quote-lists? false,
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Light",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :label-color nil,
      :scalar-mapkey-max-length 33,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :print-level-inline-results 3,
      :legacy-terminal? false,
      :quote-symbols? false,
      :colorize? true,
      :label-max-length 44,
      :margin-inline-start 0,
      :print-length-inline-results 8,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (java.util.HashSet. #{1 "a" 2 "b"}))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;199;104;35;48;2;255;249;245〠java.util.HashSet〠0〠\n〠38;5;102;48;2;255;249;245〠#{〠0〠〠38;2;122;62;157〠1〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠a〠0〠〠38;2;68;140;39〠\"〠0〠〠〠 〠0〠〠38;2;122;62;157〠2〠0〠〠〠 〠0〠〠38;2;68;140;39〠\"〠0〠〠38;2;68;140;39〠b〠0〠〠38;2;68;140;39〠\"〠0〠〠38;5;102;48;2;255;249;245〠}〠0〠")))
