CREATE TABLE words (
	id INT(11) AUTO_INCREMENT,
	value VARCHAR(128) NOT NULL UNIQUE,
    
    PRIMARY KEY(id)
);

CREATE TABLE words_synonyms (
	word_id INT(11),
    synonym_id INT(11),
	probability FLOAT,
    
    PRIMARY KEY(word_id, synonym_id),
    FOREIGN KEY (word_id) REFERENCES words(id),
    FOREIGN KEY (synonym_id) REFERENCES words(id)
);