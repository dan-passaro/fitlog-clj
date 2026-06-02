(ns fitlog.ui.lib)

(defn h2 [text]
  [:h2 {:class "text-2xl"} text])

(defn- a-week-ago []
  (new js/Date (- (.now js/Date)

                  ;; a week in milliseconds
                  ;; (* 7 24 60 60 1000)
                  604800000)))

(defn- today? [date]
  (= (.toDateString date)
     (.toDateString (new js/Date))))

(defn human-date-str [js-date]
  (let [date (new js/Date js-date)]
    (cond
      (today? date) "Today"
      (> date (a-week-ago)) (.toLocaleString date js/undefined {:weekday "long"})
      :else (.toLocaleString date js/undefined {:month "short"
                                                :weelday "long"
                                                :day "numeric"}))))
