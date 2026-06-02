(ns fitlog.test-util)

(defmacro use-fitlog-fixtures []
  `(do
     (cljs.test/use-fixtures :once {:before fitlog.router/setup-router})
     (cljs.test/use-fixtures :each {:before #(reset! fitlog.nav/app-view nil)
                                    :after fitlog.test-util/cleanup})))
