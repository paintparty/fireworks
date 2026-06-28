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
      "〠38;5;102〠[〠0〠〠38;5;61〠...〠0〠〠38;5;102〠]〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
       string/join)))))

#?(:bb nil
   :clj
   (deftest
    custom-map-datatype
    (is
     (=
      "〠38;5;102〠{〠0〠〠38;5;61〠 ...〠0〠〠〠 〠0〠〠38;5;61〠〠0〠〠38;5;102〠}〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
       string/join)))))

#?(:bb nil
   :clj
   (deftest
    vector-with-custom-datatype
    (is
     (=
      "〠38;5;102〠[〠0〠〠38;5;32〠[〠0〠〠38;5;61〠...〠0〠〠38;5;32〠]〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;5;61〠 ...〠0〠〠〠 〠0〠〠38;5;61〠〠0〠〠38;5;32〠}〠0〠〠38;5;102〠]〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
       string/join)))))

#?(:bb nil
   :clj
   (deftest
    user-fn-names
    (is
     (=
      "〠38;5;102〠{〠0〠〠38;5;97〠:user-fn〠0〠〠〠       〠0〠〠〠 〠0〠〠38;5;61〠fireworks.sample/xy〠0〠〠〠\n 〠0〠〠38;5;97〠:really-long-fn〠0〠〠〠 〠0〠〠38;5;61〠fireworks.sample/xyasldfasldkf〠0〠〠3;38;5;245〠...〠0〠〠38;5;102〠}〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
       string/join)))))

(deftest
 no-truncation
 (is
  (=
   "〠38;5;102〠(〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx〠0〠〠38;5;64〠\"〠0〠〠〠\n 〠0〠〠38;5;97〠0〠0〠〠〠\n 〠0〠〠38;5;97〠1〠0〠〠〠\n 〠0〠〠38;5;97〠2〠0〠〠〠\n 〠0〠〠38;5;97〠3〠0〠〠〠\n 〠0〠〠38;5;97〠4〠0〠〠〠\n 〠0〠〠38;5;97〠5〠0〠〠〠\n 〠0〠〠38;5;97〠6〠0〠〠〠\n 〠0〠〠38;5;97〠7〠0〠〠〠\n 〠0〠〠38;5;97〠8〠0〠〠〠\n 〠0〠〠38;5;97〠9〠0〠〠〠\n 〠0〠〠38;5;97〠10〠0〠〠〠\n 〠0〠〠38;5;97〠11〠0〠〠〠\n 〠0〠〠38;5;97〠12〠0〠〠〠\n 〠0〠〠38;5;97〠13〠0〠〠〠\n 〠0〠〠38;5;97〠14〠0〠〠〠\n 〠0〠〠38;5;97〠15〠0〠〠〠\n 〠0〠〠38;5;97〠16〠0〠〠〠\n 〠0〠〠38;5;97〠17〠0〠〠〠\n 〠0〠〠38;5;97〠18〠0〠〠〠\n 〠0〠〠38;5;97〠19〠0〠〠〠\n 〠0〠〠38;5;97〠20〠0〠〠〠\n 〠0〠〠38;5;97〠21〠0〠〠〠\n 〠0〠〠38;5;97〠22〠0〠〠〠\n 〠0〠〠38;5;97〠23〠0〠〠〠\n 〠0〠〠38;5;97〠24〠0〠〠〠\n 〠0〠〠38;5;97〠25〠0〠〠〠\n 〠0〠〠38;5;97〠26〠0〠〠〠\n 〠0〠〠38;5;97〠27〠0〠〠〠\n 〠0〠〠38;5;97〠28〠0〠〠〠\n 〠0〠〠38;5;97〠29〠0〠〠〠\n 〠0〠〠38;5;97〠30〠0〠〠〠\n 〠0〠〠38;5;97〠31〠0〠〠〠\n 〠0〠〠38;5;97〠32〠0〠〠〠\n 〠0〠〠38;5;97〠33〠0〠〠〠\n 〠0〠〠38;5;97〠34〠0〠〠〠\n 〠0〠〠38;5;97〠35〠0〠〠〠\n 〠0〠〠38;5;97〠36〠0〠〠〠\n 〠0〠〠38;5;97〠37〠0〠〠〠\n 〠0〠〠38;5;97〠38〠0〠〠〠\n 〠0〠〠38;5;97〠39〠0〠〠〠\n 〠0〠〠38;5;97〠40〠0〠〠〠\n 〠0〠〠38;5;97〠41〠0〠〠〠\n 〠0〠〠38;5;97〠42〠0〠〠〠\n 〠0〠〠38;5;97〠43〠0〠〠〠\n 〠0〠〠38;5;97〠44〠0〠〠〠\n 〠0〠〠38;5;97〠45〠0〠〠〠\n 〠0〠〠38;5;97〠46〠0〠〠〠\n 〠0〠〠38;5;97〠47〠0〠〠〠\n 〠0〠〠38;5;97〠48〠0〠〠〠\n 〠0〠〠38;5;97〠49〠0〠〠38;5;102〠)〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 string-value
 (is
  (=
   "〠38;5;64〠\"〠0〠〠38;5;64〠string〠0〠〠38;5;64〠\"〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 regex-value
 (is
  (=
   "〠3;38;5;166;48;5;231〠〠0〠〠38;5;64〠〠〠#〠0〠〠〠\"〠0〠〠〠^〠0〠〠48;5;255〠(〠0〠〠48;5;255〠?〠0〠〠48;5;255〠:〠0〠〠48;5;255〠a〠0〠〠48;5;255〠b〠0〠〠48;5;255〠c〠0〠〠38;5;249;48;5;255〠\\〠0〠〠48;5;255〠\\〠0〠〠38;5;249;48;5;255〠\\〠0〠〠48;5;255〠(〠0〠〠48;5;255〠〠0〠〠38;5;249;48;5;255〠\\〠0〠〠48;5;255〠[〠0〠〠48;5;255〠〠0〠〠48;5;255〠\\d〠0〠〠48;5;255〠)〠0〠〠〠+〠0〠〠48;5;255〠[〠0〠〠48;5;255〠^〠0〠〠48;5;255〠a〠0〠〠48;5;255〠-〠0〠〠48;5;255〠z〠0〠〠48;5;255〠0〠0〠〠48;5;255〠-〠0〠〠48;5;255〠9〠0〠〠48;5;255〠\\w〠0〠〠48;5;255〠]〠0〠〠〠*〠0〠〠〠$〠0〠〠〠|〠0〠〠〠^〠0〠〠〠f〠0〠〠〠o〠0〠〠〠o〠0〠〠〠b〠0〠〠〠a〠0〠〠〠r〠0〠〠〠{〠0〠〠〠1〠0〠〠〠}〠0〠〠〠s〠0〠〠〠?〠0〠〠〠$〠0〠〠〠\"〠0〠〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 uuid-value
 (is
  (=
   "〠3;38;5;166;48;5;231〠#uuid 〠0〠〠38;5;64〠\"4fe5d828-6444-11e8-8222-720007e40350\"〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 symbol-value
 (is
  (=
   "〠38;5;61〠mysym〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 symbol+meta-value
 (is
  (=
   "〠38;5;61〠mysym〠0〠 〠38;5;133;48;5;231〠    〠0〠〠38;5;133;48;5;231〠^{〠0〠〠38;5;133;48;5;231〠:foo〠0〠〠38;5;133;48;5;231〠 〠0〠〠38;5;133;48;5;231〠\"〠0〠〠38;5;133;48;5;231〠bar〠0〠〠38;5;133;48;5;231〠\"〠0〠〠38;5;133;48;5;231〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 boolean-value
 (is
  (=
   "〠38;5;97〠true〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 keyword-value
 (is
  (=
   "〠38;5;97〠:keyword〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 nil-value
 (is
  (=
   "〠38;5;97〠nil〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 Nan-value
 (is
  (=
   "〠38;5;97〠NaN〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 Inf-value
 (is
  (=
   "〠38;5;97〠Infinity〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 -Inf-value
 (is
  (=
   "〠38;5;97〠-Infinity〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 int-value
 (is
  (=
   "〠38;5;97〠1234〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 float-value
 (is
  (=
   "〠38;5;97〠〠38;5;97〠3.〠0〠〠38;5;97〠33〠0〠〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 core-fn
 (is
  (=
   "〠38;5;61〠clojure.core/juxt〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


#?(:bb nil
   :clj
   (deftest
    date-fn
    (is
     (=
      "〠38;5;61〠java.util.Date〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        java.util.Date)
       :formatted
       :string
       escape-sgr
       string/join)))))

(deftest
 map-value
 (is
  (=
   "〠38;5;102〠{〠0〠〠38;5;97〠:a〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠:b〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠:c〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠three〠0〠〠38;5;64〠\"〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 multiline-map
 (is
  (=
   "〠38;5;102〠{〠0〠〠38;5;97〠:a〠0〠〠〠    〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠abcdefghijklmnopqrstuv〠0〠〠38;5;64〠\"〠0〠〠〠\n 〠0〠〠38;5;97〠:ab〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠abcdefghijklmnopqrstuv12345〠0〠〠38;5;64〠\"〠0〠〠〠\n 〠0〠〠38;5;97〠:abcde〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠xyz〠0〠〠38;5;64〠\"〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 rainbow-brackets
 (is
  (=
   "〠38;5;102〠[〠0〠〠38;5;32〠[〠0〠〠38;5;172〠[〠0〠〠38;5;71〠[〠0〠〠38;5;170〠[〠0〠〠38;5;170〠]〠0〠〠38;5;71〠]〠0〠〠38;5;172〠]〠0〠〠38;5;32〠]〠0〠〠38;5;102〠]〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 vector-value
 (is
  (=
   "〠38;5;102〠[〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠38;5;102〠]〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 vector+meta-value
 (is
  (=
   "〠38;5;102〠[〠0〠 〠38;5;133;48;5;231〠    〠0〠〠38;5;133;48;5;231〠^{〠0〠〠38;5;133;48;5;231〠:meta-on-coll〠0〠〠38;5;133;48;5;231〠 〠0〠〠38;5;133;48;5;231〠1〠0〠〠38;5;133;48;5;231〠}〠0〠〠〠\n 〠0〠〠38;5;97〠:foo〠0〠〠〠\n 〠0〠〠38;5;97〠:baz〠0〠〠38;5;102〠]〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 set-value
 (is
  (=
   "〠38;5;102〠#{〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠three〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠:2〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 list-value
 (is
  (=
   "〠38;5;102〠(〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠38;5;102〠)〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 lazy-seq-value
 (is
  (=
   "〠38;5;102〠(〠0〠〠38;5;97〠0〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠〠 〠0〠〠38;5;97〠6〠0〠〠〠 〠0〠〠38;5;97〠7〠0〠〠〠 〠0〠〠38;5;97〠8〠0〠〠〠 〠0〠〠38;5;97〠9〠0〠〠38;5;102〠)〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 atom-value
 (is
  (=
   "〠3;38;5;166;48;5;231〠Atom〠0〠\n〠38;5;102〠{〠0〠〠38;5;97〠:status〠0〠〠〠 〠0〠〠38;5;97〠:ready〠0〠〠〠\n 〠0〠〠38;5;97〠:val〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;5;97〠:orange〠0〠〠〠 〠0〠〠〠 〠0〠〠38;5;97〠172〠0〠〠〠\n          〠0〠〠38;5;97〠:gray〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;97〠247〠0〠〠〠\n          〠0〠〠38;5;97〠:white〠0〠〠〠  〠0〠〠〠 〠0〠〠38;5;97〠231〠0〠〠〠\n          〠0〠〠38;5;97〠:yellow〠0〠〠〠 〠0〠〠〠 〠0〠〠38;5;97〠178〠0〠〠〠\n          〠0〠〠38;5;97〠:green〠0〠〠〠  〠0〠〠〠 〠0〠〠38;5;97〠76〠0〠〠〠\n          〠0〠〠38;5;97〠:red〠0〠〠〠    〠0〠〠〠 〠0〠〠38;5;97〠196〠0〠〠〠\n          〠0〠〠38;5;97〠:blue〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;97〠75〠0〠〠〠\n          〠0〠〠38;5;97〠:magenta〠0〠〠〠 〠0〠〠38;5;97〠171〠0〠〠〠\n          〠0〠〠38;5;97〠:purple〠0〠〠〠 〠0〠〠〠 〠0〠〠38;5;97〠141〠0〠〠〠\n          〠0〠〠38;5;97〠:olive〠0〠〠〠  〠0〠〠〠 〠0〠〠38;5;97〠106〠0〠〠〠\n          〠0〠〠38;5;97〠:black〠0〠〠〠  〠0〠〠〠 〠0〠〠38;5;97〠16〠0〠〠38;5;32〠}〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 volatile!-value
 (is
  (=
   "〠3;38;5;166;48;5;231〠Volatile〠0〠\n〠38;5;102〠{〠0〠〠38;5;97〠:status〠0〠〠〠 〠0〠〠38;5;97〠:ready〠0〠〠〠 〠0〠〠38;5;97〠:val〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 volatile
 (is
  (=
   "〠3;38;5;166;48;5;231〠Volatile〠0〠\n〠38;5;102〠{〠0〠〠38;5;97〠:status〠0〠〠〠 〠0〠〠38;5;97〠:ready〠0〠〠〠 〠0〠〠38;5;97〠:val〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 transient-vector2
 (is
  (=
   "〠3;38;5;166;48;5;231〠TransientVector〠0〠\n〠38;5;102〠{〠0〠〠3;38;5;245〠〠〠...+4〠0〠〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 transient-set2
 (is
  (=
   "〠3;38;5;166;48;5;231〠TransientHashSet〠0〠〠〠#{}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 transient-map2
 (is
  (=
   "〠3;38;5;166;48;5;231〠TransientArrayMap〠0〠\n〠38;5;102〠{〠0〠〠3;38;5;245〠 〠〠...+2〠0〠〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 single-line-coll-print-length-50-19
 (is
  (=
   "〠38;5;102〠(〠0〠〠38;5;97〠0〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠〠 〠0〠〠38;5;97〠6〠0〠〠〠 〠0〠〠38;5;97〠7〠0〠〠〠 〠0〠〠38;5;97〠8〠0〠〠〠 〠0〠〠38;5;97〠9〠0〠〠〠 〠0〠〠38;5;97〠10〠0〠〠〠 〠0〠〠38;5;97〠11〠0〠〠〠 〠0〠〠38;5;97〠12〠0〠〠〠 〠0〠〠38;5;97〠13〠0〠〠38;5;102〠)〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 single-line-coll-print-length-50-20
 (is
  (=
   "〠38;5;102〠(〠0〠〠38;5;97〠0〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠〠 〠0〠〠38;5;97〠6〠0〠〠〠 〠0〠〠38;5;97〠7〠0〠〠〠 〠0〠〠38;5;97〠8〠0〠〠〠 〠0〠〠38;5;97〠9〠0〠〠〠 〠0〠〠38;5;97〠10〠0〠〠〠 〠0〠〠38;5;97〠11〠0〠〠〠 〠0〠〠38;5;97〠12〠0〠〠〠 〠0〠〠38;5;97〠13〠0〠〠〠 〠0〠〠38;5;97〠14〠0〠〠38;5;102〠)〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 array-map-order
 (is
  (=
   "〠38;5;102〠{〠0〠〠38;5;97〠:a〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠\n 〠0〠〠38;5;97〠:b〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠\n 〠0〠〠38;5;97〠:c〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠\n 〠0〠〠38;5;97〠:d〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠\n 〠0〠〠38;5;97〠:e〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠〠\n 〠0〠〠38;5;97〠:f〠0〠〠〠 〠0〠〠38;5;97〠6〠0〠〠〠\n 〠0〠〠38;5;97〠:g〠0〠〠〠 〠0〠〠38;5;97〠7〠0〠〠〠\n 〠0〠〠38;5;97〠:h〠0〠〠〠 〠0〠〠38;5;97〠8〠0〠〠〠\n 〠0〠〠38;5;97〠:i〠0〠〠〠 〠0〠〠38;5;97〠9〠0〠〠〠\n 〠0〠〠38;5;97〠:j〠0〠〠〠 〠0〠〠38;5;97〠10〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 symbol-with-meta
 (is
  (=
   "〠38;5;61〠mysym〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 rainbow-brackets
 (is
  (=
   "〠38;5;102〠[〠0〠〠38;5;32〠[〠0〠〠38;5;172〠[〠0〠〠38;5;71〠[〠0〠〠38;5;170〠[〠0〠〠38;5;170〠]〠0〠〠38;5;71〠]〠0〠〠38;5;172〠]〠0〠〠38;5;32〠]〠0〠〠38;5;102〠]〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 rainbow-brackets-low-contrast
 (is
  (=
   "〠38;5;245〠[〠0〠〠38;5;74〠[〠0〠〠38;5;179〠[〠0〠〠38;5;106〠[〠0〠〠38;5;177〠[〠0〠〠38;5;177〠]〠0〠〠38;5;106〠]〠0〠〠38;5;179〠]〠0〠〠38;5;74〠]〠0〠〠38;5;245〠]〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


#?(:bb nil
   :clj
   (deftest
    with-scalar-level-1-depth-print-length
    (is
     (=
      "〠38;5;102〠[〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44〠0〠〠3;38;5;245〠...〠0〠〠38;5;64〠\"〠0〠〠38;5;102〠]〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        ["asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas"])
       :formatted
       :string
       escape-sgr
       string/join)))))

#?(:bb nil
   :clj
   (deftest
    scalar-result-max-length
    (is
     (=
      "〠38;5;64〠\"〠0〠〠38;5;64〠asdfffaaaaasdfasdfasdfasdfasdfasdfasdfa〠0〠...〠38;5;64〠\"〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        "asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44asdffffffas")
       :formatted
       :string
       escape-sgr
       string/join)))))

#?(:bb nil
   :clj
   (deftest
    datatype-value
    (is
     (=
      "〠3;38;5;166;48;5;231〠fireworks.sample.MyType〠0〠\n〠38;5;102〠{〠0〠〠38;5;61〠a〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠\n 〠0〠〠38;5;61〠b〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠\n 〠0〠〠38;5;61〠c〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠\n 〠0〠〠38;5;61〠d〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠\n 〠0〠〠38;5;61〠e〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠〠\n 〠0〠〠38;5;61〠f〠0〠〠〠 〠0〠〠38;5;97〠6〠0〠〠〠\n 〠0〠〠38;5;61〠g〠0〠〠〠 〠0〠〠38;5;97〠7〠0〠〠〠\n 〠0〠〠38;5;61〠h〠0〠〠〠 〠0〠〠38;5;97〠8〠0〠〠〠\n 〠0〠〠38;5;61〠i〠0〠〠〠 〠0〠〠38;5;97〠9〠0〠〠〠\n 〠0〠〠38;5;61〠j〠0〠〠〠 〠0〠〠38;5;97〠10〠0〠〠〠\n 〠0〠〠38;5;61〠k〠0〠〠〠 〠0〠〠38;5;97〠11〠0〠〠38;5;102〠}〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        fireworks.sample/my-data-type)
       :formatted
       :string
       escape-sgr
       string/join)))))

#?(:bb nil
   :clj
   (deftest
    record-value
    (is
     (=
      "〠3;38;5;166;48;5;231〠fireworks.sample.MyRecordType〠0〠\n〠38;5;102〠{〠0〠〠38;5;97〠:a〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠a〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠:b〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠bbbbbbbbbbbbbb〠0〠〠38;5;64〠\"〠0〠〠38;5;102〠}〠0〠"
      (->
       (?
        :data
        {:regex-theme :neutral,
         :line-height 1.45,
         :find nil,
         :single-column-maps? false,
         :supports-color-level 2,
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
         :elide-branches #{:bb},
         :metadata-position "inline",
         :print-detected-color-level? false,
         :bracket-contrast "high"}
        fireworks.sample/my-record-type)
       :formatted
       :string
       escape-sgr
       string/join)))))

(deftest
 java-interop-types
 (is
  (=
   "〠38;5;102〠{〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠Java collection types〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;5;61〠java.util.ArrayList〠0〠〠〠 〠0〠〠3;38;5;166;48;5;231〠java.util.ArrayList〠0〠\n                                              〠38;5;172〠[〠0〠〠38;5;97〠0〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠38;5;172〠]〠0〠〠〠\n                          〠0〠〠38;5;61〠java.util.HashMap〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;5;166;48;5;231〠java.util.HashMap〠0〠\n                                              〠38;5;172〠{〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠a〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠b〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠38;5;172〠}〠0〠〠〠\n                          〠0〠〠38;5;61〠java.util.HashSet〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;5;166;48;5;231〠java.util.HashSet〠0〠\n                                              〠38;5;172〠#{〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠a〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠b〠0〠〠38;5;64〠\"〠0〠〠38;5;172〠}〠0〠〠〠\n                          〠0〠〠38;5;61〠java.lang.String〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠welcome〠0〠〠38;5;64〠\"〠0〠〠〠\n                          〠0〠〠38;5;61〠array〠0〠〠〠              〠0〠〠〠 〠0〠〠3;38;5;166;48;5;231〠Ljava.lang.Object〠0〠\n                                              〠38;5;172〠[〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠 〠0〠〠38;5;97〠4〠0〠〠〠 〠0〠〠38;5;97〠5〠0〠〠38;5;172〠]〠0〠〠38;5;32〠}〠0〠〠〠\n 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠Java numbers〠0〠〠38;5;64〠\"〠0〠〠〠         〠0〠〠〠 〠0〠〠38;5;32〠{〠0〠〠38;5;97〠:ratio〠0〠〠〠               〠0〠〠〠 〠0〠〠38;5;97〠1/3〠0〠〠〠\n                          〠0〠〠38;5;97〠:byte〠0〠〠〠                〠0〠〠〠 〠0〠〠38;5;97〠0〠0〠〠〠\n                          〠0〠〠38;5;97〠:short〠0〠〠〠               〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠〠\n                          〠0〠〠38;5;97〠:double〠0〠〠〠              〠0〠〠〠 〠0〠〠38;5;97〠〠38;5;97〠23.〠0〠〠38;5;97〠44〠0〠〠0〠〠〠\n                          〠0〠〠38;5;97〠:decimal〠0〠〠〠             〠0〠〠〠 〠0〠〠38;5;97〠1M〠0〠〠〠\n                          〠0〠〠38;5;97〠:int〠0〠〠〠                 〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠\n                          〠0〠〠38;5;97〠:float〠0〠〠〠               〠0〠〠〠 〠0〠〠38;5;97〠〠38;5;97〠1.〠0〠〠38;5;97〠5〠0〠〠0〠〠〠\n                          〠0〠〠38;5;97〠:char〠0〠〠〠                〠0〠〠〠 〠0〠〠〠a〠0〠〠〠\n                          〠0〠〠38;5;97〠:java.math.BigInteger〠0〠〠〠 〠0〠〠38;5;97〠171〠0〠〠38;5;32〠}〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 java-util-hashmap
 (is
  (=
   "〠3;38;5;166;48;5;231〠java.util.HashMap〠0〠\n〠38;5;102〠{〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠a〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠b〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 java-util-arraylist
 (is
  (=
   "〠3;38;5;166;48;5;231〠java.util.ArrayList〠0〠\n〠38;5;102〠[〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;97〠3〠0〠〠38;5;102〠]〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))


(deftest
 java-util-hashset
 (is
  (=
   "〠3;38;5;166;48;5;231〠java.util.HashSet〠0〠\n〠38;5;102〠#{〠0〠〠38;5;97〠1〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠a〠0〠〠38;5;64〠\"〠0〠〠〠 〠0〠〠38;5;97〠2〠0〠〠〠 〠0〠〠38;5;64〠\"〠0〠〠38;5;64〠b〠0〠〠38;5;64〠\"〠0〠〠38;5;102〠}〠0〠"
   (->
    (?
     :data
     {:regex-theme :neutral,
      :line-height 1.45,
      :find nil,
      :single-column-maps? false,
      :supports-color-level 2,
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
    string/join))))
