(ns fitlog.util)

(defn get!
  "Like get, but throw an error on missing key"
  [map key]
  (if (contains? map key)
    (get map key)
    (throw (ex-info (str "Missing key: " key)
                    {:key key}
                    :key-missing))))
