(ns fetch-cc-data.main
  (:gen-class)
  (:require
   [fetch-cc-data.core :as fetch-cc-data]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.cli :as cli]))


(def cli-options
  [["-t" "--top N" "Number of top coins."
    :default 10
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 10000) "Must be a number between 1 and 10000."]]
   ["-d" "--days N" "Number of days of history."
    :default 30
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 10000) "Must be a number between 1 and 10000."]]
   ["-f" "--folder PATH" "Target folder."
    :default "resources/coin_data"
    :validate [#(.isDirectory (io/file %)) "Must be an existing folder."]]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->> ["Usage: fetch-cc-data [options]"
        ""
        "Options:"
        options-summary
        ""]
       (string/join \newline)))

(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating if the the program should take action and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      :else
      {:action true :options options})))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn -main
  [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (let [{:keys [top days folder]} options]
        (println "Fetching top" top "coins data...")
        (fetch-cc-data/overall-processing! top days folder)
        (println (inc top) "files written in" folder)))))
