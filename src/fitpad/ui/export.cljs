;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitpad.ui.export
  (:require
   [fitpad.data :as d]))

(defn- send-file-to-user [filename content]
  (let [blob (js/Blob. #js [content])
        url (js/URL.createObjectURL blob)
        link (js/document.createElement "a")]
    (set! (.-href link) url)
    (set! (.-download link) filename)
    (.click link)

    (js/setTimeout (fn [] (js/URL.revokeObjectURL url))
                   60000)))

(defn ^:async export-fitpad-data []
  (send-file-to-user "fitpad-data.json"
                     (js/JSON.stringify (clj->js @d/data)
                                        nil
                                        "    ")))
