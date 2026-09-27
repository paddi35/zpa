declare
  cursor cur is select * from emp;
  type t_tab is table of emp%rowtype;
  v_tab t_tab;
  v_row emp%rowtype;
begin
  open cur;
  fetch cur bulk collect into v_tab; -- Noncompliant {{Add a LIMIT clause to this BULK COLLECT to avoid loading an unbounded number of rows into memory.}}
  close cur;

  open cur;
  fetch cur bulk collect into v_tab limit 100; -- ok, has a LIMIT clause
  close cur;

  open cur;
  fetch cur into v_row; -- ok, no BULK COLLECT at all
  close cur;
end;
/
