(ns fitlog.test-util)

(defmacro use-fitlog-fixtures [& {:keys [each once]
                                  :or {each [] once []}}]
  `(do
     (cljs.test/use-fixtures :once {:before fitlog.router/setup-router}
       ~@once)
     (cljs.test/use-fixtures :each {:before #(reset! fitlog.nav/app-view nil)
                                    :after fitlog.test-util/cleanup}
       ~@each)))

(defmacro deftest-async [name & body]
  `(cljs.test/deftest ~(vary-meta name assoc :async true)
     (try
       ~@body
       (catch :default e#
         (cljs.test/is false (str "async test threw: " e#))))))

(defmacro with-mock-date [date-val & body]
  `(do
     (.set fitlog.test-util/mock-date (fitlog.test-util/default-to-noon ~date-val))
     (try
       ~@body
       (finally
         (.reset fitlog.test-util/mock-date)))))
