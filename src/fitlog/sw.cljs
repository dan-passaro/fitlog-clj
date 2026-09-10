;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.sw
  "The Service Worker definition for Fitlog.")

(defonce timeout (atom nil))

(defn- ^:async app-focused? []
  (let [clients (await (js/self.clients.matchAll
                        #js {:type "window"
                             :includeUncontrolled true}))]
    (some (fn [c] (and (= (.-visibilityState c) "visible")
                       (.-focused c)))
          clients)))

(defn- start-rest-timer! [^js evt]
  (js/clearTimeout @timeout)
  (reset! timeout nil)
  (let [ends-at (.. evt -data -endsAt)]
    (.waitUntil
     evt
     (js/Promise.
      (fn [resolve]
        (js/setTimeout
         (^:async fn []
          (when-not (await (app-focused?))
            (js/self.registration.showNotification
             "Rest complete"
             #js {:body "Time for the next set"
                  :tag "rest-timer"}))
          (reset! timeout nil)
          (resolve))
         (- (js/Date. ends-at) (js/Date.))))))))

(defn init []
  (js/self.addEventListener "fetch" (fn []))
  (js/self.addEventListener
   "message"
   (fn [^js evt]
     (case (.. evt -data -type)
       "START_REST_TIMER" (start-rest-timer! evt)))))
