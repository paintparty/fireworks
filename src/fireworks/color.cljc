(ns fireworks.color 
  (:require [clojure.string :as string]))

(defn trunc [v]
  (if (neg? v)
    #?(:cljs
       (js/Math.ceil v)
       :clj
       (Math/ceil v))
    #?(:cljs
       (js/Math.floor v)
       :clj
       (Math/floor v))))

(defn ci [c]
  (cond
    (< c 48)  0
    (< c 115) 1
    :else     (trunc (/ (- c 35) 40))))

(defn dist-sq [x, y, z, r, g, b]
  (+ (* (- x r) (- x r))
     (* (- y g) (- y g))
     (* (- z b) (- z b))))

(defn rgb->x256 [r g b]
 (let [ri       (ci r)
       gi       (ci g)
       bi       (ci b)
       clr-i    (+ (* 36 ri) (* 6 gi) bi)
       avg      (trunc (/ (+ r g b)  3))
       gry-i    (if (> avg 238) 23 (trunc (/ (- avg 3) 10)))
       hexes    [0, 0x5F, 0x87, 0xAF, 0xD7, 0xFF]
       rc       (nth hexes ri)
       gc       (nth hexes gi)
       bc       (nth hexes bi)
       gry      (+ 8 (* 10 gry-i))
       clr-dist (dist-sq rc, gc, bc, r, g, b)
       gry-dist (dist-sq gry, gry, gry, r, g, b)
       ret      (if (<= clr-dist gry-dist)
                  (+ 16 clr-i) 
                  (+ 232 gry-i))
       ret      #?(:cljs
                   (js/Math.round ret)
                   :clj
                   (do
                     #_(println ret (type ret) (int? ret))
                     (cond (double? ret)
                           (Math/round ret)
                           :else
                           ret)))]
   ret))

(defn hexa->rgb*
  "Converts a hexa color (string) to an [r g b] vector representation."
  [s]
  (let [s (if (contains? #{4 5} (count s)) 
            (let [base (subs s 1)]
              (str "#" base base))
            s)
        [_ r1 r2 g1 g2 b1 b2 a1 a2] s
        f #?(:cljs #(js/parseInt (str "0x" %1 %2))
             :clj #(Integer/valueOf (str %1 %2) 16))
        r (f r1 r2)
        g (f g1 g2)
        b (f b1 b2)
        a (when (and a1 a2)
            (let [v (f a1 a2)]
              #?(:cljs v
                 :clj (Double/parseDouble (format "%.2f"
                                                  (double (/ v 255)))))))]
    [[r g b] a]))

(defn hexa->rgb
  "Converts a hexa color (string) to an [r g b] vector representation."
  [s]
  (-> s hexa->rgb* (nth 0 nil)))

(defn hexa->rgba
  "Converts a hexa color (string) to an [r g b a] vector representation."
  [s]
  (let [ret* (hexa->rgb* s)
        rgb  (nth ret* 0 nil)
        a    (nth ret* 1 nil)]
    (conj rgb (if a a 1))))

(defn hexa->x256 [s]
  (->> s hexa->rgb (apply rgb->x256)))

(defn ->fixed-float [points n]
  #?(:cljs
     (.toFixed (.parseFloat js/Number n) points)
     :clj
     (format (str "%." points "f") (double n))))

;; ;; TODO - test this
;; (defn hex->hsl [s]
;;   (let [[r g b] (hexa->rgb s)
;;         r       (/ r 255)
;;         g       (/ g 255)
;;         b       (/ b 255)
;;         cmin    (min r g b)
;;         cmax    (max r g b)
;;         delta   (- cmax cmin)
;;         h       0
;;         s       0
;;         l       0
;;         h       (cond (= delta 0) 0
;;                       (= cmax r)  (mod (/ (- g b) delta) 6)
;;                       (= cmax g)  (/ (- b r) (+ delta 2))
;;                       :else       (/ (- b r) (+ delta 4)))
;;         h       #?(:cljs
;;                    (js/Math.round (* h 60))
;;                    :clj
;;                    (Math/round (* h 60)))
;;         h       (if (< h 0) (+ h 360) h)
;;         l       (/ (+ cmax cmin) 2)
;;         s       (if (= delta 0) 0 (/ delta (- 1 (abs (* 2 (- l 1))))))
;;         s       (->fixed-float s 1)
;;         l       (->fixed-float l 1)]
;;     (str "hsl(" h "," s "%," l "%)")))

(def xterm-colors-by-id
   {32  "#0087d7",
    64  "#5f8700",
    96  "#875f87",
    128 "#af00d7",
    160 "#d70000",
    192 "#d7ff87",
    224 "#ffd7d7",
    33  "#0087ff",
    65  "#5f875f",
    97  "#875faf",
    129 "#af00ff",
    161 "#d7005f",
    193 "#d7ffaf",
    225 "#ffd7ff",
    34  "#00af00",
    66  "#5f8787",
    98  "#875fd7",
    130 "#af5f00",
    162 "#d70087",
    194 "#d7ffd7",
    226 "#ffff00",
    35  "#00af5f",
    67  "#5f87af",
    99  "#875fff",
    131 "#af5f5f",
    163 "#d700af",
    195 "#d7ffff",
    227 "#ffff5f",
    36  "#00af87",
    68  "#5f87d7",
    100 "#878700",
    132 "#af5f87",
    164 "#d700d7",
    196 "#ff0000",
    228 "#ffff87",
    37  "#00afaf",
    69  "#5f87ff",
    101 "#87875f",
    133 "#af5faf",
    165 "#d700ff",
    197 "#ff005f",
    229 "#ffffaf",
    38  "#00afd7",
    70  "#5faf00",
    102 "#878787",
    134 "#af5fd7",
    166 "#d75f00",
    198 "#ff0087",
    230 "#ffffd7",
    39  "#00afff",
    71  "#5faf5f",
    103 "#8787af",
    135 "#af5fff",
    167 "#d75f5f",
    199 "#ff00af",
    231 "#ffffff",
    40  "#00d700",
    72  "#5faf87",
    104 "#8787d7",
    136 "#af8700",
    168 "#d75f87",
    200 "#ff00d7",
    232 "#080808",
    41  "#00d75f",
    73  "#5fafaf",
    105 "#8787ff",
    137 "#af875f",
    169 "#d75faf",
    201 "#ff00ff",
    233 "#121212",
    42  "#00d787",
    74  "#5fafd7",
    106 "#87af00",
    138 "#af8787",
    170 "#d75fd7",
    202 "#ff5f00",
    234 "#1c1c1c",
    43  "#00d7af",
    75  "#5fafff",
    107 "#87af5f",
    139 "#af87af",
    171 "#d75fff",
    203 "#ff5f5f",
    235 "#262626",
    44  "#00d7d7",
    76  "#5fd700",
    108 "#87af87",
    140 "#af87d7",
    172 "#d78700",
    204 "#ff5f87",
    236 "#303030",
    45  "#00d7ff",
    77  "#5fd75f",
    109 "#87afaf",
    141 "#af87ff",
    173 "#d7875f",
    205 "#ff5faf",
    237 "#3a3a3a",
    46  "#00ff00",
    78  "#5fd787",
    110 "#87afd7",
    142 "#afaf00",
    174 "#d78787",
    206 "#ff5fd7",
    238 "#444444",
    47  "#00ff5f",
    79  "#5fd7af",
    111 "#87afff",
    143 "#afaf5f",
    175 "#d787af",
    207 "#ff5fff",
    239 "#4e4e4e",
    16  "#000000",
    48  "#00ff87",
    80  "#5fd7d7",
    112 "#87d700",
    144 "#afaf87",
    176 "#d787d7",
    208 "#ff8700",
    240 "#585858",
    17  "#00005f",
    49  "#00ffaf",
    81  "#5fd7ff",
    113 "#87d75f",
    145 "#afafaf",
    177 "#d787ff",
    209 "#ff875f",
    241 "#626262",
    18  "#000087",
    50  "#00ffd7",
    82  "#5fff00",
    114 "#87d787",
    146 "#afafd7",
    178 "#d7af00",
    210 "#ff8787",
    242 "#6c6c6c",
    19  "#0000af",
    51  "#00ffff",
    83  "#5fff5f",
    115 "#87d7af",
    147 "#afafff",
    179 "#d7af5f",
    211 "#ff87af",
    243 "#767676",
    20  "#0000d7",
    52  "#5f0000",
    84  "#5fff87",
    116 "#87d7d7",
    148 "#afd700",
    180 "#d7af87",
    212 "#ff87d7",
    244 "#808080",
    21  "#0000ff",
    53  "#5f005f",
    85  "#5fffaf",
    117 "#87d7ff",
    149 "#afd75f",
    181 "#d7afaf",
    213 "#ff87ff",
    245 "#8a8a8a",
    22  "#005f00",
    54  "#5f0087",
    86  "#5fffd7",
    118 "#87ff00",
    150 "#afd787",
    182 "#d7afd7",
    214 "#ffaf00",
    246 "#949494",
    23  "#005f5f",
    55  "#5f00af",
    87  "#5fffff",
    119 "#87ff5f",
    151 "#afd7af",
    183 "#d7afff",
    215 "#ffaf5f",
    247 "#9e9e9e",
    24  "#005f87",
    56  "#5f00d7",
    88  "#870000",
    120 "#87ff87",
    152 "#afd7d7",
    184 "#d7d700",
    216 "#ffaf87",
    248 "#a8a8a8",
    25  "#005faf",
    57  "#5f00ff",
    89  "#87005f",
    121 "#87ffaf",
    153 "#afd7ff",
    185 "#d7d75f",
    217 "#ffafaf",
    249 "#b2b2b2",
    26  "#005fd7",
    58  "#5f5f00",
    90  "#870087",
    122 "#87ffd7",
    154 "#afff00",
    186 "#d7d787",
    218 "#ffafd7",
    250 "#bcbcbc",
    27  "#005fff",
    59  "#5f5f5f",
    91  "#8700af",
    123 "#87ffff",
    155 "#afff5f",
    187 "#d7d7af",
    219 "#ffafff",
    251 "#c6c6c6",
    28  "#008700",
    60  "#5f5f87",
    92  "#8700d7",
    124 "#af0000",
    156 "#afff87",
    188 "#d7d7d7",
    220 "#ffd700",
    252 "#d0d0d0",
    29  "#00875f",
    61  "#5f5faf",
    93  "#8700ff",
    125 "#af005f",
    157 "#afffaf",
    189 "#d7d7ff",
    221 "#ffd75f",
    253 "#dadada",
    30  "#008787",
    62  "#5f5fd7",
    94  "#875f00",
    126 "#af0087",
    158 "#afffd7",
    190 "#d7ff00",
    222 "#ffd787",
    254 "#e4e4e4",
    31  "#0087af",
    63  "#5f5fff",
    95  "#875f5f",
    127 "#af00af",
    159 "#afffff", 
    191 "#d7ff5f", 
    223 "#ffd7af", 
    255 "#eeeeee"})

(def named-html-colors
  {"aliceblue"            "#f0f8ff"	
   "antiquewhite"         "#faebd7"	
   "aqua"                 "#00ffff"	
   "aquamarine"           "#7fffd4"	
   "azure"                "#f0ffff"	
   "beige"                "#f5f5dc"	
   "bisque"               "#ffe4c4"	
   "black"                "#000000"	
   "blanchedalmond"       "#ffebcd"	
   "blue"                 "#0000ff"	
   "blueviolet"           "#8a2be2"	
   "brown"                "#a52a2a"	
   "burlywood"            "#deb887"	
   "cadetblue"            "#5f9ea0"	
   "chartreuse"           "#7fff00"	
   "chocolate"            "#d2691e"	
   "coral"                "#ff7f50"	
   "cornflowerblue"       "#6495ed"	
   "cornsilk"             "#fff8dc"	
   "crimson"              "#dc143c"	
   "cyan"                 "#00ffff" 
   "darkblue"             "#00008b"	
   "darkcyan"             "#008b8b"	
   "darkgoldenrod"        "#b8860b"	
   "darkgray"             "#a9a9a9"	
   "darkgreen"            "#006400"	
   "darkgrey"             "#a9a9a9"	
   "darkkhaki"            "#bdb76b"	
   "darkmagenta"          "#8b008b"	
   "darkolivegreen"       "#556b2f"	
   "darkorange"           "#ff8c00"	
   "darkorchid"           "#9932cc"	
   "darkred"              "#8b0000"	
   "darksalmon"           "#e9967a"	
   "darkseagreen"         "#8fbc8f"	
   "darkslateblue"        "#483d8b"	
   "darkslategray"        "#2f4f4f"	
   "darkslategrey"        "#2f4f4f"	
   "darkturquoise"        "#00ced1"	
   "darkviolet"           "#9400d3"	
   "deeppink"             "#ff1493"	
   "deepskyblue"          "#00bfff"	
   "dimgray"              "#696969"	
   "dimgrey"              "#696969"	
   "dodgerblue"           "#1e90ff"	
   "firebrick"            "#b22222"	
   "floralwhite"          "#fffaf0"	
   "forestgreen"          "#228b22"	
   "fuchsia"              "#ff00ff"	
   "gainsboro"            "#dcdcdc"	
   "ghostwhite"           "#f8f8ff"	
   "gold"                 "#ffd700"	
   "goldenrod"            "#daa520"	
   "gray"                 "#808080"	
   "green"                "#008000"	
   "greenyellow"          "#adff2f"	
   "grey"                 "#808080" 
   "honeydew"             "#f0fff0"	
   "hotpink"              "#ff69b4"	
   "indianred"            "#cd5c5c"	
   "indigo"               "#4b0082"	
   "ivory"                "#fffff0"	
   "khaki"                "#f0e68c"	
   "lavender"             "#e6e6fa"	
   "lavenderblush"        "#fff0f5"	
   "lawngreen"            "#7cfc00"	
   "lemonchiffon"         "#fffacd"	
   "lightblue"            "#add8e6"	
   "lightcoral"           "#f08080"	
   "lightcyan"            "#e0ffff"	
   "lightgoldenrodyellow" "#fafad2"	
   "lightgray"            "#d3d3d3"	
   "lightgreen"           "#90ee90"	
   "lightgrey"            "#d3d3d3"	
   "lightpink"            "#ffb6c1"	
   "lightsalmon"          "#ffa07a"	
   "lightseagreen"        "#20b2aa"	
   "lightskyblue"         "#87cefa"	
   "lightslategray"       "#778899"	
   "lightslategrey"       "#778899"	
   "lightsteelblue"       "#b0c4de"	
   "lightyellow"          "#ffffe0"	
   "lime"                 "#00ff00"	
   "limegreen"            "#32cd32"	
   "linen"                "#faf0e6"	
   "magenta"              "#ff00ff" 
   "maroon"               "#800000"	
   "mediumaquamarine"     "#66cdaa"	
   "mediumblue"           "#0000cd"	
   "mediumorchid"         "#ba55d3"	
   "mediumpurple"         "#9370db"	
   "mediumseagreen"       "#3cb371"	
   "mediumslateblue"      "#7b68ee"	
   "mediumspringgreen"    "#00fa9a"	
   "mediumturquoise"      "#48d1cc"	
   "mediumvioletred"      "#c71585"	
   "midnightblue"         "#191970"	
   "mintcream"            "#f5fffa"	
   "mistyrose"            "#ffe4e1"	
   "moccasin"             "#ffe4b5"	
   "navajowhite"          "#ffdead"	
   "navy"                 "#000080"	
   "oldlace"              "#fdf5e6"	
   "olive"                "#808000"	
   "olivedrab"            "#6b8e23"	
   "orange"               "#ffa500"	
   "orangered"            "#ff4500"	
   "orchid"               "#da70d6"	
   "palegoldenrod"        "#eee8aa"	
   "palegreen"            "#98fb98"	
   "paleturquoise"        "#afeeee"	
   "palevioletred"        "#db7093"	
   "papayawhip"           "#ffefd5"	
   "peachpuff"            "#ffdab9"	
   "peru"                 "#cd853f"	
   "pink"                 "#ffc0cb"	
   "plum"                 "#dda0dd"	
   "powderblue"           "#b0e0e6"	
   "purple"               "#800080"	
   "rebeccapurple"        "#663399"	
   "red"                  "#ff0000"	
   "rosybrown"            "#bc8f8f"	
   "royalblue"            "#4169e1"	
   "saddlebrown"          "#8b4513"	
   "salmon"               "#fa8072"	
   "sandybrown"           "#f4a460"	
   "seagreen"             "#2e8b57"	
   "seashell"             "#fff5ee"	
   "sienna"               "#a0522d"	
   "silver"               "#c0c0c0"	
   "skyblue"              "#87ceeb"	
   "slateblue"            "#6a5acd"	
   "slategray"            "#708090"	
   "slategrey"            "#708090"	
   "snow"                 "#fffafa"	
   "springgreen"          "#00ff7f"	
   "steelblue"            "#4682b4"	
   "tan"                  "#d2b48c"	
   "teal"                 "#008080"	
   "thistle"              "#d8bfd8"	
   "tomato"               "#ff6347"	
   "transparent"          "#00000000"	
   "turquoise"            "#40e0d0"	
   "violet"               "#ee82ee"	
   "wheat"                "#f5deb3"	
   "white"                "#ffffff"	
   "whitesmoke"           "#f5f5f5"	
   "yellow"               "#ffff00"	
   "yellowgreen"          "#9acd32"})
