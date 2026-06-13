(ns fitlog.test-util)

(defmacro use-fitlog-fixtures [& {:keys [each once]
                                  :or {each [] once []}}]
  `(do
     (cljs.test/use-fixtures :once {:before fitlog.test-util/test-suite-setup}
       ~@once)
     (cljs.test/use-fixtures :each {:before #(reset! fitlog.nav/app-view nil)
                                    :after fitlog.test-util/cleanup}
       ~@each)))

(defmacro deftest-async [name & body]
  `(cljs.test/deftest ~(vary-meta name assoc :async true)
     (try
       ~@body
       (catch :default e#


         ;; Don't output stacktrace for TestingLibraryElementError, because the
         ;; stacktrace is useless and the error message is massive and the
         ;; stacktrace causes it to be printed twice.
         (if (not= (.-name e#) "TestingLibraryElementError")

           ;; this will output a stacktrace, although it's kind of useless
           ;; because it doesn't use source maps and points to opaque JS
           (js/console.error "Async test threw:" e#))

         (cljs.test/is false (str "async test threw: " e#))))))

(defmacro with-mock-date [date-val & body]
  `(do
     (.set fitlog.test-util/mock-date (fitlog.test-util/default-to-noon ~date-val))
     (try
       ~@body
       (finally
         (.reset fitlog.test-util/mock-date)))))
