(ns fitlog.ui.navbar
  (:require
   ["@heroicons/react/24/outline" :refer [Bars3Icon]]
   [fitlog.data :as d]
   [fitlog.ui.lib :refer [icon-btn]]
   [fitlog.ui.rest-timer :refer [rest-timer]]))

(defn- menu-item [text onclick]
  [:button {:type "button" :on-click onclick} text])

(defn- select-import-file []
  (-> (.getElementById js/document "import-file-input")
      .click))

(defn- ^:async perform-import-file [evt]
  (let [file (-> evt .-target .-files (aget 0))
        text (await (.text file))]
    (reset! d/data (-> text
                       js/JSON.parse
                       (js->clj :keywordize-keys true)
                       d/migrate))))

(defn navbar []
  [:div {:class "navbar bg-base-200 shadow-sm sticky top-0 z-50"}
   [:div {:class "navbar-start"}
    [:div {:class "dropdown"}
     [icon-btn {:icon Bars3Icon
                :label "Tools menu"
                :size "2em"}]
     [:ul {:class "menu menu-sm dropdown-content bg-base-200 w-52 p-2 mt-3 shadow"}
      [:li
       [menu-item "Import file" select-import-file]
       [:input {:type "file"
                :id "import-file-input"
                :hidden true
                :on-change perform-import-file}]]]]
    [:span {:class "text-xl"}
     "Fitlog"]]
   [:div {:class "navbar-end"}
    [rest-timer]]])
