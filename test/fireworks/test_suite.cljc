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
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Dark",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :scalar-mapkey-max-length 20,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :legacy-terminal? false,
         :quote-symbols? false,
         :label-max-length 44,
         :margin-inline-start 0,
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
      "〠3;38;2;167;148;206;48;2;43;22;97〠fireworks.sample.CustomVector〠0〠\n〠38;5;250;48;2;43;22;97〠[〠0〠〠38;2;110;171;237〠...〠0〠〠38;5;250;48;2;43;22;97〠]〠0〠"))))

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
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Dark",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :scalar-mapkey-max-length 20,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :legacy-terminal? false,
         :quote-symbols? false,
         :label-max-length 44,
         :margin-inline-start 0,
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
      "〠3;38;2;167;148;206;48;2;43;22;97〠fireworks.sample.CustomMap〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;110;171;237〠 ...〠0〠〠〠 〠0〠〠38;2;110;171;237〠〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠"))))

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
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Dark",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :scalar-mapkey-max-length 20,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :legacy-terminal? false,
         :quote-symbols? false,
         :label-max-length 44,
         :margin-inline-start 0,
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
      "〠38;5;250〠[〠0〠〠3;38;2;167;148;206;48;2;43;22;97〠fireworks.sample.CustomVector〠0〠\n 〠38;5;38;48;2;43;22;97〠[〠0〠〠38;2;110;171;237〠...〠0〠〠38;5;38;48;2;43;22;97〠]〠0〠〠〠\n 〠0〠〠3;38;2;167;148;206;48;2;43;22;97〠fireworks.sample.CustomMap〠0〠\n 〠38;5;38;48;2;43;22;97〠{〠0〠〠38;2;110;171;237〠 ...〠0〠〠〠 〠0〠〠38;2;110;171;237〠〠0〠〠38;5;38;48;2;43;22;97〠}〠0〠〠38;5;250〠]〠0〠"))))

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
         :scalar-depth-1-max-length 69,
         :margin-left 0,
         :bold? false,
         :margin-top 0,
         :print-level 7,
         :single-line-coll-max-length 33,
         :format-label-as-code? false,
         :single-column-map-threshold 44,
         :scalar-result-max-length 444,
         :theme "Alabaster Dark",
         :metadata-print-level 4,
         :scalar-max-length 33,
         :scalar-mapkey-max-length 20,
         :print-length 40,
         :multi-line-metadata? true,
         :truncate? true,
         :enable-terminal-font-weights? true,
         :dissoc-metadata-keys nil,
         :legacy-terminal? false,
         :quote-symbols? false,
         :label-max-length 44,
         :margin-inline-start 0,
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
      "〠38;5;250〠{〠0〠〠38;2;182;150;181〠:user-fn〠0〠〠〠       〠0〠〠〠 〠0〠〠38;2;110;171;237〠fireworks.sample/xy〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:really-long-fn〠0〠〠〠 〠0〠〠38;2;110;171;237〠xyasldfasldkfaslkjfzzzzzzzzzzz〠0〠〠38;2;210;140;109〠...〠0〠〠38;5;250〠}〠0〠"))))

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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 40,
      :multi-line-metadata? true,
      :truncate? false,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠(〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠adsfasdfasdfasdfasdfadsfsdfasdfadsfadsfasdfasdfasdfadsfasdfasdfsadfxxx〠0〠〠38;2;210;140;109〠\"〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠0〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠1〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠2〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠3〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠4〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠5〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠6〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠7〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠8〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠9〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠10〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠11〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠12〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠13〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠14〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠15〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠16〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠17〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠18〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠19〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠20〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠21〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠22〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠23〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠24〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠25〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠26〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠27〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠28〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠29〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠30〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠31〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠32〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠33〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠34〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠35〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠36〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠37〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠38〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠39〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠40〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠41〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠42〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠43〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠44〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠45〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠46〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠47〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠48〠0〠〠〠\n 〠0〠〠38;2;110;171;237〠49〠0〠〠38;5;250〠)〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠string〠0〠〠38;2;210;140;109〠\"〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 100,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠〠0〠〠38;2;140;189;122〠〠〠#〠0〠〠〠\"〠0〠〠〠^〠0〠〠48;2;57;57;57〠(〠0〠〠48;2;57;57;57〠?〠0〠〠48;2;57;57;57〠:〠0〠〠48;2;57;57;57〠a〠0〠〠48;2;57;57;57〠b〠0〠〠48;2;57;57;57〠c〠0〠〠38;2;128;128;128;48;2;57;57;57〠\\〠0〠〠48;2;57;57;57〠\\〠0〠〠38;2;128;128;128;48;2;57;57;57〠\\〠0〠〠48;2;57;57;57〠(〠0〠〠48;2;57;57;57〠〠0〠〠38;2;128;128;128;48;2;57;57;57〠\\〠0〠〠48;2;57;57;57〠[〠0〠〠48;2;57;57;57〠〠0〠〠48;2;57;57;57〠\\d〠0〠〠48;2;57;57;57〠)〠0〠〠〠+〠0〠〠48;2;57;57;57〠[〠0〠〠48;2;57;57;57〠^〠0〠〠48;2;57;57;57〠a〠0〠〠48;2;57;57;57〠-〠0〠〠48;2;57;57;57〠z〠0〠〠48;2;57;57;57〠0〠0〠〠48;2;57;57;57〠-〠0〠〠48;2;57;57;57〠9〠0〠〠48;2;57;57;57〠\\w〠0〠〠48;2;57;57;57〠]〠0〠〠〠*〠0〠〠〠$〠0〠〠〠|〠0〠〠〠^〠0〠〠〠f〠0〠〠〠o〠0〠〠〠o〠0〠〠〠b〠0〠〠〠a〠0〠〠〠r〠0〠〠〠{〠0〠〠〠1〠0〠〠〠}〠0〠〠〠s〠0〠〠〠?〠0〠〠〠$〠0〠〠〠\"〠0〠〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 100,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠#uuid 〠0〠〠38;2;140;189;122〠\"4fe5d828-6444-11e8-8222-720007e40350\"〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠mysym〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠mysym〠0〠 〠38;2;174;132;155;48;2;58;18;40〠    〠0〠〠38;2;174;132;155;48;2;58;18;40〠^{〠0〠〠38;2;174;132;155;48;2;58;18;40〠:foo〠0〠〠38;2;174;132;155;48;2;58;18;40〠 〠0〠〠38;2;174;132;155;48;2;58;18;40〠\"〠0〠〠38;2;174;132;155;48;2;58;18;40〠bar〠0〠〠38;2;174;132;155;48;2;58;18;40〠\"〠0〠〠38;2;174;132;155;48;2;58;18;40〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;182;150;181〠true〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;182;150;181〠:keyword〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;182;150;181〠nil〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠NaN〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠Infinity〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠-Infinity〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠1234〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠〠38;2;110;171;237〠3.〠0〠〠38;2;110;171;237〠33〠0〠〠0〠")))


(deftest
 lambda
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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
      :display-metadata? false,
      :margin-bottom 1,
      :metadata-position "inline",
      :print-detected-color-level? false,
      :bracket-contrast "high"}
     (fn []))
    :formatted
    :string
    escape-sgr
    string/join)
   "〠3;38;2;167;148;206;48;2;43;22;97〠λ〠0〠〠38;2;110;171;237〠〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠clojure.core/juxt〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠java.util.Date〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠{〠0〠〠38;2;182;150;181〠:a〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;182;150;181〠:b〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;182;150;181〠:c〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠three〠0〠〠38;2;210;140;109〠\"〠0〠〠38;5;250〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠{〠0〠〠38;2;182;150;181〠:a〠0〠〠〠    〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠abcdefghijklmnopqrstuv〠0〠〠38;2;210;140;109〠\"〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:ab〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠abcdefghijklmnopqrstuv12345〠0〠〠38;2;210;140;109〠\"〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:abcde〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠xyz〠0〠〠38;2;210;140;109〠\"〠0〠〠38;5;250〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠[〠0〠〠38;5;38〠[〠0〠〠38;5;142〠[〠0〠〠38;5;77〠[〠0〠〠38;5;170〠[〠0〠〠38;5;170〠]〠0〠〠38;5;77〠]〠0〠〠38;5;142〠]〠0〠〠38;5;38〠]〠0〠〠38;5;250〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠[〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠38;5;250〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250;48;2;58;18;40〠[〠0〠 〠38;2;174;132;155;48;2;58;18;40〠    〠0〠〠38;2;174;132;155;48;2;58;18;40〠^{〠0〠〠38;2;174;132;155;48;2;58;18;40〠:meta-on-coll〠0〠〠38;2;174;132;155;48;2;58;18;40〠 〠0〠〠38;2;174;132;155;48;2;58;18;40〠1〠0〠〠38;2;174;132;155;48;2;58;18;40〠}〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:foo〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:baz〠0〠〠38;5;250;48;2;58;18;40〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠#{〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠three〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;182;150;181〠:2〠0〠〠38;5;250〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠(〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠38;5;250〠)〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠(〠0〠〠38;2;110;171;237〠0〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠〠 〠0〠〠38;2;110;171;237〠6〠0〠〠〠 〠0〠〠38;2;110;171;237〠7〠0〠〠〠 〠0〠〠38;2;110;171;237〠8〠0〠〠〠 〠0〠〠38;2;110;171;237〠9〠0〠〠38;5;250〠)〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠Atom〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;182;150;181〠:status〠0〠〠〠 〠0〠〠38;2;182;150;181〠:ready〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:val〠0〠〠〠   〠0〠〠〠 〠0〠〠38;5;38〠{〠0〠〠38;2;182;150;181〠:black〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;110;171;237〠16〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:blue〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;110;171;237〠75〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:gray〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;110;171;237〠247〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:green〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;110;171;237〠76〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:magenta〠0〠〠〠 〠0〠〠38;2;110;171;237〠171〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:olive〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;110;171;237〠106〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:orange〠0〠〠〠 〠0〠〠〠 〠0〠〠38;2;110;171;237〠172〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:purple〠0〠〠〠 〠0〠〠〠 〠0〠〠38;2;110;171;237〠141〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:red〠0〠〠〠    〠0〠〠〠 〠0〠〠38;2;110;171;237〠196〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:white〠0〠〠〠  〠0〠〠〠 〠0〠〠38;2;110;171;237〠231〠0〠〠〠\n          〠0〠〠38;2;182;150;181〠:yellow〠0〠〠〠 〠0〠〠〠 〠0〠〠38;2;110;171;237〠178〠0〠〠38;5;38〠}〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠Volatile〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;182;150;181〠:status〠0〠〠〠 〠0〠〠38;2;182;150;181〠:ready〠0〠〠〠 〠0〠〠38;2;182;150;181〠:val〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠Volatile〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;182;150;181〠:status〠0〠〠〠 〠0〠〠38;2;182;150;181〠:ready〠0〠〠〠 〠0〠〠38;2;182;150;181〠:val〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠TransientVector〠0〠\n〠38;5;250;48;2;43;22;97〠[〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠38;5;250;48;2;43;22;97〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠TransientHashSet〠0〠\n〠38;5;250;48;2;43;22;97〠#{〠0〠〠38;2;210;140;109〠〠〠...+2〠0〠〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠TransientArrayMap〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;210;140;109〠 〠〠...+2〠0〠〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 50,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠(〠0〠〠38;2;110;171;237〠0〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠〠 〠0〠〠38;2;110;171;237〠6〠0〠〠〠 〠0〠〠38;2;110;171;237〠7〠0〠〠〠 〠0〠〠38;2;110;171;237〠8〠0〠〠〠 〠0〠〠38;2;110;171;237〠9〠0〠〠〠 〠0〠〠38;2;110;171;237〠10〠0〠〠〠 〠0〠〠38;2;110;171;237〠11〠0〠〠〠 〠0〠〠38;2;110;171;237〠12〠0〠〠〠 〠0〠〠38;2;110;171;237〠13〠0〠〠38;5;250〠)〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 50,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠(〠0〠〠38;2;110;171;237〠0〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠〠 〠0〠〠38;2;110;171;237〠6〠0〠〠〠 〠0〠〠38;2;110;171;237〠7〠0〠〠〠 〠0〠〠38;2;110;171;237〠8〠0〠〠〠 〠0〠〠38;2;110;171;237〠9〠0〠〠〠 〠0〠〠38;2;110;171;237〠10〠0〠〠〠 〠0〠〠38;2;110;171;237〠11〠0〠〠〠 〠0〠〠38;2;110;171;237〠12〠0〠〠〠 〠0〠〠38;2;110;171;237〠13〠0〠〠〠 〠0〠〠38;2;110;171;237〠14〠0〠〠38;5;250〠)〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠{〠0〠〠38;2;182;150;181〠:a〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:b〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:c〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:d〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:e〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:f〠0〠〠〠 〠0〠〠38;2;110;171;237〠6〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:g〠0〠〠〠 〠0〠〠38;2;110;171;237〠7〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:h〠0〠〠〠 〠0〠〠38;2;110;171;237〠8〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:i〠0〠〠〠 〠0〠〠38;2;110;171;237〠9〠0〠〠〠\n 〠0〠〠38;2;182;150;181〠:j〠0〠〠〠 〠0〠〠38;2;110;171;237〠10〠0〠〠38;5;250〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;110;171;237〠mysym〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠[〠0〠〠38;5;38〠[〠0〠〠38;5;142〠[〠0〠〠38;5;77〠[〠0〠〠38;5;170〠[〠0〠〠38;5;170〠]〠0〠〠38;5;77〠]〠0〠〠38;5;142〠]〠0〠〠38;5;38〠]〠0〠〠38;5;250〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;245〠[〠0〠〠38;5;67〠[〠0〠〠38;5;137〠[〠0〠〠38;5;64〠[〠0〠〠38;5;134〠[〠0〠〠38;5;134〠]〠0〠〠38;5;64〠]〠0〠〠38;5;137〠]〠0〠〠38;5;67〠]〠0〠〠38;5;245〠]〠0〠")))


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
      :scalar-depth-1-max-length 60,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠[〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠asdfffaaaaasdfasdfasdfasdfasdfasdfasdfaaaafasdfasdfff44〠0〠〠38;2;210;140;109〠...〠0〠〠38;2;210;140;109〠\"〠0〠〠38;5;250〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 44,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠asdfffaaaaasdfasdfasdfasdfasdfasdfasdfa〠0〠...〠38;2;210;140;109〠\"〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠fireworks.sample.MyType〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;110;171;237〠 ...〠0〠〠〠 〠0〠〠38;2;110;171;237〠〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠fireworks.sample.MyRecordType〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;182;150;181〠:a〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠a〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;182;150;181〠:b〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠bbbbbbbbbbbbbb〠0〠〠38;2;210;140;109〠\"〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 100,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠38;5;250〠{〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠Java collection〠0〠〠38;2;210;140;109〠...〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;5;38〠{〠0〠〠38;2;110;171;237〠java.util.ArrayList〠0〠〠〠 〠0〠〠3;38;2;167;148;206;48;2;43;22;97〠java.util.ArrayList〠0〠\n                                           〠38;5;142;48;2;43;22;97〠[〠0〠〠38;2;110;171;237〠0〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠38;5;142;48;2;43;22;97〠]〠0〠〠〠\n                       〠0〠〠38;2;110;171;237〠java.util.HashMap〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;167;148;206;48;2;43;22;97〠java.util.HashMap〠0〠\n                                           〠38;5;142;48;2;43;22;97〠{〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠a〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠b〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠38;5;142;48;2;43;22;97〠}〠0〠〠〠\n                       〠0〠〠38;2;110;171;237〠java.util.HashSet〠0〠〠〠  〠0〠〠〠 〠0〠〠3;38;2;167;148;206;48;2;43;22;97〠java.util.HashSet〠0〠\n                                           〠38;5;142;48;2;43;22;97〠#{〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠a〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠b〠0〠〠38;2;210;140;109〠\"〠0〠〠38;5;142;48;2;43;22;97〠}〠0〠〠〠\n                       〠0〠〠38;2;110;171;237〠java.lang.String〠0〠〠〠   〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠welcome〠0〠〠38;2;210;140;109〠\"〠0〠〠〠\n                       〠0〠〠38;2;110;171;237〠array〠0〠〠〠              〠0〠〠〠 〠0〠〠3;38;2;167;148;206;48;2;43;22;97〠Ljava.lang.Object〠0〠\n                                           〠38;5;142;48;2;43;22;97〠[〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠 〠0〠〠38;2;110;171;237〠4〠0〠〠〠 〠0〠〠38;2;110;171;237〠5〠0〠〠38;5;142;48;2;43;22;97〠]〠0〠〠38;5;38〠}〠0〠〠〠\n 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠Java numbers〠0〠〠38;2;210;140;109〠\"〠0〠〠〠      〠0〠〠〠 〠0〠〠38;5;38〠{〠0〠〠38;2;182;150;181〠:ratio〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;110;171;237〠1/3〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:byte〠0〠〠〠               〠0〠〠〠 〠0〠〠38;2;110;171;237〠0〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:short〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:double〠0〠〠〠             〠0〠〠〠 〠0〠〠38;2;110;171;237〠〠38;2;110;171;237〠23.〠0〠〠38;2;110;171;237〠44〠0〠〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:decimal〠0〠〠〠            〠0〠〠〠 〠0〠〠38;2;110;171;237〠1M〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:int〠0〠〠〠                〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:float〠0〠〠〠              〠0〠〠〠 〠0〠〠38;2;110;171;237〠〠38;2;110;171;237〠1.〠0〠〠38;2;110;171;237〠5〠0〠〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:char〠0〠〠〠               〠0〠〠〠 〠0〠〠〠a〠0〠〠〠\n                       〠0〠〠38;2;182;150;181〠:java.math.BigInt〠0〠〠38;2;210;140;109〠...〠0〠〠〠 〠0〠〠38;2;110;171;237〠171〠0〠〠38;5;38〠}〠0〠〠38;5;250〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠java.util.HashMap〠0〠\n〠38;5;250;48;2;43;22;97〠{〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠a〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠b〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠java.util.ArrayList〠0〠\n〠38;5;250;48;2;43;22;97〠[〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;110;171;237〠3〠0〠〠38;5;250;48;2;43;22;97〠]〠0〠")))


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
      :scalar-depth-1-max-length 69,
      :margin-left 0,
      :bold? false,
      :margin-top 0,
      :print-level 7,
      :single-line-coll-max-length 33,
      :format-label-as-code? false,
      :single-column-map-threshold 44,
      :scalar-result-max-length 444,
      :theme "Alabaster Dark",
      :metadata-print-level 4,
      :scalar-max-length 33,
      :scalar-mapkey-max-length 20,
      :print-length 33,
      :multi-line-metadata? true,
      :truncate? true,
      :enable-terminal-font-weights? true,
      :dissoc-metadata-keys nil,
      :legacy-terminal? false,
      :quote-symbols? false,
      :label-max-length 44,
      :margin-inline-start 0,
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
   "〠3;38;2;167;148;206;48;2;43;22;97〠java.util.HashSet〠0〠\n〠38;5;250;48;2;43;22;97〠#{〠0〠〠38;2;110;171;237〠1〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠a〠0〠〠38;2;210;140;109〠\"〠0〠〠〠 〠0〠〠38;2;110;171;237〠2〠0〠〠〠 〠0〠〠38;2;210;140;109〠\"〠0〠〠38;2;140;189;122〠b〠0〠〠38;2;210;140;109〠\"〠0〠〠38;5;250;48;2;43;22;97〠}〠0〠")))
