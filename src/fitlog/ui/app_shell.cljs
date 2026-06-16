(ns fitlog.ui.app-shell
  (:require
   [fitlog.nav :refer [app-view]]
   [fitlog.ui.rest-timer :refer [rest-timer]]))

(defn app-shell []
  [:div {:class "container mx-auto px-4 flex flex-col items-center text-center gap-4"}
   [rest-timer]
   (if-let [view (:view (:data @app-view))]
     (let [params (:path-params @app-view)]
       [view params])
     [:p "Unknown route"])])
