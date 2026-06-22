(ns fitlog.remote-storage
  (:require
   ["remotestoragejs" :as RemoteStorage]))

(defonce instance
  (delay
    (doto (RemoteStorage.)
      (-> .-access (.claim "fitlog" "rw"))
      (-> .-caching (.enable "/fitlog/"))
      (.setApiKeys
       #js
       {:googledrive "374510084269-3klcvo30tbdp3c6c7690bsll8dsnnmo8.apps.googleusercontent.com"
        :dropbox "d9n168tv0fanyg5"}))))

(defonce client
  (delay
    (.scope @instance "/fitlog/")))

(defn init! []
  @instance)

(defn ^:async write-file [& {:keys [path content mime-type]
                             :or {mime-type "text/plain"}}]
  (.storeFile ^js @client mime-type path content))

(defn ^:async read-file [path]
  (.-data (await (.getFile ^js @client path))))
