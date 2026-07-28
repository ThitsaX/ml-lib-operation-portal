ALTER TABLE tbl_participant ADD participant_type varchar(100) NULL;
ALTER TABLE tbl_participant CHANGE participant_type participant_type varchar(100) NULL AFTER parent_participant_name;
