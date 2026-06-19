(ns fitlog.ui.rest-timer
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   ["@heroicons/react/24/outline" :refer [ClockIcon]]
   [reagent.core :as r]
   [tick.core :as t]
   [fitlog.ui.lib :refer [icon]]))

(defonce interval-id (atom nil))
(defonce now-tick (r/atom (t/now)))
(defonce timer-end (r/atom nil))

;; Apparently tick doesn't come with a browser-friendly formatter. Its formatter
;; requires js-joda/locale_en-us, which seems to require a bunch of Node.js
;; stdlib modules. So do it manually here.
(defn fmt-timer
  "Turn a tick duration into an 'MM:SS' string."
  [duration]
  (let [total (long (/ (t/millis duration) 1000))
        m (quot (mod total 3600) 60)
        s (mod total 60)]
    (gstring/format "%d:%02d" m s)))

(defn rest-timer []
  (when-let [time-end @timer-end]
     (let [now @now-tick]
       [:<>
        [:div {:class "flex flex-none ml-auto items-center gap-1"}
         [icon ClockIcon]
         (fmt-timer (t/between now time-end))]])))

(defn- stop-ticking! []
  (when-let [id @interval-id]
    (js/clearInterval id)
    (reset! interval-id nil)))

(defn- tick []
  (let [now (t/now)]
    (reset! now-tick now)
    (when-let [rest-end @timer-end]
      (when (t/>= now rest-end)
        (reset! timer-end nil)
        (stop-ticking!)))))

(defn- start-ticking! []
  (when-not @interval-id
    (reset! interval-id (js/setInterval tick 1000))))

(defn start-rest-timer! [duration]
  (stop-ticking!)  ;; cleanup existing timer, if any
  (reset! timer-end (t/>> (t/now) duration))
  (reset! now-tick (t/now))
  (start-ticking!))
