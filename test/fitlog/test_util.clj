(ns fitlog.test-util)

(defmacro use-fitlog-fixtures [& {:keys [each once]
                                  :or {each [] once []}}]
  `(do
     (cljs.test/use-fixtures :once {:before fitlog.router/setup-router}
       ~@once)
     (cljs.test/use-fixtures :each {:before #(reset! fitlog.nav/app-view nil)
                                    :after fitlog.test-util/cleanup}
       ~@each)))
