(ns fitlog.ui.remote-storage
  (:require
   ["react" :as react]
   [reagent.core :as r]
   ["remotestorage-widget" :as RsWidget]
   ["remotestoragejs" :as RemoteStorage]))

(defonce rs-instance
  (let [rs (RemoteStorage.)]
    (-> rs .-access (.claim "fitlog" "rw"))
    (-> rs .-caching (.enable "/fitlog/"))
    rs))

(defn- enable-cloud-sync [el]
  (let [widget (RsWidget. rs-instance)]
    (.attach widget el)))

(defn remote-storage-btn []
  (let [el-ref (react/createRef)]
    (r/create-class
     {:display-name "remote-storage-btn"

      :component-did-mount
      #(enable-cloud-sync (.-current el-ref))

      :reagent-render
      (fn []
        [:div {:class "fixed bottom-4 right-4 z-50"
               :ref el-ref}])})))
