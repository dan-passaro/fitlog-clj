(ns fitlog.ui.app-shell
  (:require
   [fitlog.nav :refer [app-view]]
   [fitlog.ui.navbar :refer [navbar]]
   [fitlog.ui.remote-storage :refer [remote-storage-btn]]))

(defn app-shell []
  [:div {:class "container mx-auto px-4 flex flex-col items-center text-center gap-4"}
   [navbar]
   (if-let [view (:view (:data @app-view))]
     (let [params (:path-params @app-view)]
       [view params])
     [:p "Unknown route"])
   [remote-storage-btn]])
