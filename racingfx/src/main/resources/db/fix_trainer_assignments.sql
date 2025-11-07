-- Fix for missing trainer assignments
-- This script addresses the issue where winning horses have no trainers

-- Option 1: Add a trainer for stable2 (the stable of the winning horse Formula One)
INSERT INTO Trainer (trainerId, lname, fname, stableId)
VALUES ('trainer9', 'Hassan', 'Omar', 'stable2');

-- Option 2: Or, if you prefer to reassign Formula One to an existing trainer's stable
-- Uncomment the line below to move Formula One to stable1 (which has trainers)
-- UPDATE Horse SET stableId = 'stable1' WHERE horseId = 'horse10';

-- Verify the fix - this should now show trainers with winning horses
SELECT t.fname, t.lname, h.horseName, rr.results, rr.prize
FROM Trainer t
INNER JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId
INNER JOIN RaceResults rr ON rr.horseId = h.horseId
WHERE LOWER(TRIM(rr.results)) IN ('first', '1st', '1', 'win', 'winner')
ORDER BY rr.prize DESC;

-- Check total winnings per trainer
SELECT t.fname, t.lname, COALESCE(SUM(rr.prize), 0.0) AS totalWinnings
FROM Trainer t
LEFT JOIN Horse h ON CAST(t.stableId AS CHAR(30)) = h.stableId
LEFT JOIN RaceResults rr ON rr.horseId = h.horseId
GROUP BY t.trainerId, t.fname, t.lname
ORDER BY totalWinnings DESC;
