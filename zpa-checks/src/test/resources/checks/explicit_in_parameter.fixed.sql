create or replace procedure foo(bar in number) is -- Noncompliant {{Explicitly declare this parameter as IN.}}
--                              ^^^^^^^^^^
begin
  null;
end;
/
CREATE OR REPLACE PROCEDURE FOO(BAR IN NUMBER, BAZ IN VARCHAR2 DEFAULT 'X') IS -- Noncompliant
-- Noncompliant@-1
BEGIN
  NULL;
END;
/
create or replace function foo(bar
                               in varchar2) return number is -- Noncompliant @-1
begin
  return 1;
end;
/
create or replace procedure foo(p1 in number,
                                p2 out number,
                                p3 in out number) is
  cursor cur(pcur number) is -- cursor parameters cannot raise a violation
    select 1
      from dual;
begin
  null;
end;
/
