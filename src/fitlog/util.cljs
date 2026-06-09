(ns fitlog.util)

(defn get!
  "Like get, but throw an error on missing key"
  [map key]
  (if (contains? map key)
    (get map key)
    (throw (ex-info (str "Missing key: " key)
                    {:key key}
                    :key-missing))))

(defn not-implemented-error []
  (ex-info "Not implemented" {} :not-implemented))

(defn vec-dissoc [coll i]
  (vec (concat (subvec coll 0 i)
               (subvec coll (inc i)))))

(defn vec-insert [coll i item]
  (vec (concat (subvec coll 0 i)
               [item]
               (subvec coll i))))
