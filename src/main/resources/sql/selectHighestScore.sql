SELECT uuid, distance, mapname FROM highscores WHERE mapname=? ORDER by mapname, distance DESC LIMIT 1
