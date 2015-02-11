(ns media-library.test
  (:require [clojure.string :as s]
            [media-library.file :as file] ))

(def keywords ["Musical dynamics" "Time signatures" "DNA-binding proteins" "Making a mural"  "Polyphony" "American battle rifles" "Quantum mechanics" "Anatomy of a water molecule"   "Musical modes" "Ways to save pennys" "Chinese art and culture" "Writing your own music" "Philosophy of religion" "Aristotle's philosophy"  "Architecture" "Art and Design" "Biology" "Business"  "Business management" "Chemistry""Computing" "Cymraeg" "Dance" "Design and manufacture" "Design and Technology"  "Drama" "Electronics" "Engineering" "Engineering science" "English" "English Literature" "Expressive Arts"  "Fashion and textile technology" "Food Technology" "French" "Gaelic" "Geography" "German" "Graphic communication" "Graphics" "Health and food technology" "Health and wellbeing" "History" "Hospitality" "ICT" "Italian" "Lifeskills Maths" "Literacy and English" "Mandarin" "Maths" "Media Studies" "Modern Foreign Languages" "Modern Languages" "Modern Studies" "Music" "Music Technology" "People in society, economy and business" "People, past events and societies" "People, place and environment" "Physical Education" "Physics" "PSHE and Citizenship" "Religious and moral education" "Religious Studies" "Religious, moral and philosophical studies" "Resistant Materials" "Welsh Literature"])

(def genres [ "Adventure" "Animation" "Comedy" "Crime" "Documentary" "Drama" "Family" "Fantasy" "Foreign" "History" "Horror" "Music" "Mystery" "Romance" "Sci-Fi" "Fantasy" "Science" "Fiction" "Series" "Thriller" "War" "Western"])

(defn series  [i] 
  [["Series 1"
    [{:name "All Episodes",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "/1.mkv" )}]]
   ["Series 2"
    [{:name "Episodes 01",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "/2/1.mkv" )}
     {:name "Episodes 02",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "2/2.mkv" )}
     {:name "Episodes 03",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "2/3.mkv" )}
     {:name "Episodes 04",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "2/4.mkv" )}
     {:name "Episodes 05",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "2/5.mkv" )}
     {:name "Episodes 06",
      :type ".mkv",
      :path (str  "/Video/Tv/" i "2/6.mkv" )}]]])

(def data 
  {:details
   {:spoken_languages ["English"],
    :release_date "2013-03-06",
    :genres ["Comedy" "Drama"],
    :overview
    "Electrick Children tells the story of Rachel, a rambunctious girl from a polygamist colony in southern Utah. On Rachel’s 15th birthday, she finds a forbidden cassette tape. Having never seen anything like it before, Rachel plays the cassette tape, and finds glorious rock &amp; roll thereupon. Weeks later, Rachel realizes a miracle has occurred- and the cassette tape must have something to do with it. She leaves her family and runs away to the closest city: Las Vegas. There she searches for the singer of the band on the cassette tape. She has a wild adventure and ultimately discovers who she really is: an ELECTRICK CHILD.",
    :keywords [],
    :title "Electrick Children",
    :imdb_id "tt2139843",
    :runtime 96,
    :links
    {:poster_path "/gmpd7azilLkaHPWgAlGpLyM6Fp.jpg",
     :self "movie/89237",
     :poster nil,
     :keywords "movie/89237/keywords",
     :images "movie/89237/images",
     :videos "movie/89237/videos",
     :details "movie/89237"}},
   :name "Electrick Children",
   :type ".m4v",
   :path "/Video/Movies/3/Electrick Children-1.m4v"}
  )

(def names ["Gisele Groll" "Kristofer Kirkley" "Carlton Clickner" "Nida Nyquist" "Katherine Kowalski" "Valerie Vogl" "Caren Carlberg" "Arielle Antle" "Chanell Cowherd" "Layla Lindenberg" "Ellamae Eichhorn" "Merissa Maisonet" "Lupita Lanoue" "Altha Arias" "Charline Clowers" "Sonya Suen" "Sherman Shifflett" "Jewell Janelle" "Anamaria Aguiar" "Laronda Lund" "Emerson Eure" "Kasi Kober" "Elenor Ebling" "Shila Sharlow" "Ocie Oslund" "Oleta Overfield" "Wynell Wyckoff" "Angie Addy" "Marilynn Micheal" "Dennis Driscoll" "Kayla Kobel" "Joya Jessie" "Suzette Sankey" "Mariel Mourer" "Dorthy Derossett" "Gilbert Gates" "Alfred Aitchison" "Marget Mcginty" "Elvira Eckart" "Davida Decicco" "Shaunna Stanek" "Nelda Niemann" "Eunice Edenfield" "Iola Ishmael" "Nichelle Nale" "Kimberly Kearley" "Derrick Danek" "Vella Venne" "Luanne Laprade" "Bobby Berkey"])

(defn paragraphs [] (filter (complement  empty?)  (s/split (slurp "text.txt") #"\n")))
(def ps (paragraphs) )

(defn rand-choices [max col]
  (set  (map (fn [_] (rand-nth col))  (range 1 (+ 1  (int (rand max)))))))

(defn transform-data [i] 
  (let [path (str "files/movies/" i)
        paragraphs ps
        nums       ["01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12"] ]
    (-> data
        (update-in [:path] (fn [_] path))
        (update-in [:details :title] (fn [_] (nth names i)))
        (update-in [:name] (fn [_] (nth names i)))
        (update-in [:details :runtime] (fn [_] (rand-nth (range 89 135))))
        (update-in [:details :overview] (fn [_] (nth paragraphs i)))
        (update-in [:details :release_date] (fn [_] (str "20" (rand-nth nums) "-" (rand-nth nums) "-" (rand-nth nums))))
        (update-in [:details :links :poster_path] (fn [_] (str "/" i ".jpg")))
        (update-in [:details :keywords] (fn [_](rand-choices 5 keywords) ))
        (update-in [:details :genres] (fn [_](rand-choices 3 genres) ))
        ((fn [d] (if (zero? (mod i 3)) 
                   (do (-> d (assoc  :series (series i)) 
                             (update-in  [:details :genres] (fn [e] (set  (conj e "Series")))))) 
                   d)))
        ((fn [d] [path d])))))

(file/write-edn-file "data"  (into {}  (map transform-data (range 1 46))))

