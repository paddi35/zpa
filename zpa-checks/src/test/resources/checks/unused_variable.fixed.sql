declare
  i number; -- Noncompliant {{Remove this unused "I" local variable.}}
  
  procedure proc is
  begin
    null;
  end;
  
  function func return number is
  begin
    null;
  end;
begin
  null;
  
  declare
  begin
    null;
  end;
  
  for i in 1..2 loop -- this "i" variable is not the same as the one in declaration section
    func(i);
  end loop;
end;
/

create or replace package body test is
  hidden_var number;  -- Noncompliant {{Remove this unused "HIDDEN_VAR" local variable.}}
  "VAR" number; -- Noncompliant {{Remove this unused "VAR" local variable.}}
  "var" number;
  
  procedure proc is
    hidden_var number; -- this declaration hides the previous one
    var number; -- this declaration hides the previous "VAR"
  begin
    hidden_var := 0;
    var := 0;
    "var" := 0;
  end;
end;
/

declare
  -- documented but unused
  -- Noncompliant@-1
  used number;
begin
  used := 1;
end;
/

-- noncompliant code without a quick fix
declare
  call_default number := f(1); -- Noncompliant
  unknown_default date := sysdate; -- Noncompliant
begin
  <<blk>>
  declare
    qualified number; -- Noncompliant
  begin
    blk.qualified := 1;
  end;
end;
/

-- correct
declare
  simple_var number;
  into_var number;
  insert_var number;
  comparision_var number;
  case_insensitive_test number;
  record_variable rectype;
  error exception;
begin
  simple_var := 0;
  CASE_INSENSITIVE_TEST := 0;
  record_variable.field := 0;
  
  select 1
    into into_var
    from dual;
    
  insert into tab (x) values (insert_var);
  
  if (comparision_var = 1) then
    null;
  end if;
  
  for i in 1..2 loop -- "i" variable is not used, but it is required here
    null;
  end loop;
  
  raise error;
end;
/

create or replace package test is
  package_body_var number; -- do not report error on package specifications
end;
/

create or replace package body test is
  package_body_var number;
  qualified_var number;
  
  procedure proc is
  begin
    package_body_var := 0;
    test.qualified_var := 0;
    test.var := 0;
  end;
end;
/
