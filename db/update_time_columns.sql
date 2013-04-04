ALTER TABLE request ALTER column begin_lease TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE request ALTER column end_lease TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE valid_procedure_time ALTER column start_time TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE valid_procedure_time ALTER column end_time TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE observation ALTER column phenomenon_time_start TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE observation ALTER column phenomenon_time_end TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE observation ALTER column result_time TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE observation ALTER column valid_time_start TYPE TIMESTAMP WITH TIME ZONE;
ALTER TABLE observation ALTER column valid_time_end TYPE TIMESTAMP WITH TIME ZONE;