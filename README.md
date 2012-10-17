lat-nam
=======

lat-nam was is an island in magic lore that used to be home of the College of Lat-Nam where a group of wizards amassed more knowledge then had ever (and probably will ever be) amassed.
This seemed like a good name for a database... :P

Description
-----------

lat-nam is a supposed to fill the need for an api that gatherer doesn't provide. It does this by scraping gatherer and storing the info in a mongodb database and providing a way for anyone to access that data.

What's Done
-----------

* Scraper
* Find API page

TO-DO
-----

* Image API
* Figure out what language the older cards are

Example
-------

Lets say you want to get the cards name "Black Lotus".
http://www.lat-nam.com/Find?query={name:"Black Lotus"}
Which gives you this:
	[{"cmc":0,"text":"Adds 3 mana of any single color of your choice to your mana pool, then is discarded. Tapping this artifact can be played as an interrupt.","expansion":"limited edition alpha","_id":{"$oid":"506c877f7d1e722ddbc87aac"},"name":"Black Lotus","_types":"Mono Artifact","rarity":"rare","multiverseid":3,"_words":["black","lotus","adds","3","mana","of","any","single","color","your","choice","to","pool","then","is","discarded","tapping","this","artifact","can","be","played","as","an","interrupt","null","mono","limited","edition","alpha","0"],"artist":"Christopher Rush","types":["mono","artifact"],"cost":{"colorless":0}},{"cmc":0,"text":"Adds 3 mana of any single color of your choice to your mana pool, then is discarded. Tapping this artifact can be played as an interrupt.","expansion":"limited edition beta","_id":{"$oid":"506c8ebd7d1e722ddbc87bd4"},"name":"Black Lotus","_types":"Mono Artifact","rarity":"rare","multiverseid":298,"_words":["black","lotus","adds","3","mana","of","any","single","color","your","choice","to","pool","then","is","discarded","tapping","this","artifact","can","be","played","as","an","interrupt","null","mono","limited","edition","beta","0"],"artist":"Christopher Rush","types":["mono","artifact"],"cost":{"colorless":0}},{"cmc":0,"text":"Adds 3 mana of any single color of your choice to your mana pool, then is discarded. Tapping this artifact can be played as an interrupt.","expansion":"unlimited edition","_id":{"$oid":"506c95047d1e722ddbc87d02"},"name":"Black Lotus","_types":"Mono Artifact","rarity":"rare","multiverseid":600,"_words":["black","lotus","adds","3","mana","of","any","single","color","your","choice","to","pool","then","is","discarded","tapping","this","artifact","can","be","played","as","an","interrupt","null","mono","unlimited","edition","0"],"artist":"Christopher Rush","types":["mono","artifact"],"cost":{"colorless":0}}]
As you can see you get some json with 3 cards. Most the fields are pretty self explanitory. The weird ones are _words, which can be used to do searches on all the text in the card like finding everything that has flying or affects flying. _types is just the way it is seen on the card (more so if the tokenizer changes, the types can be recalculated).
