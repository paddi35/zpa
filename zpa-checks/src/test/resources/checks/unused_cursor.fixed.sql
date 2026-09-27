declare
    
  cursor "cur2" is
    select 1 from dual;
begin
  open "cur2";
end;

declare
  cursor used_cursor is select 1 from dual;
begin
  open used_cursor;
end;

-- noncompliant code without a quick fix
declare
  cursor cur is -- Noncompliant
    select 1 from dual;
begin
  <<blk>>
  declare
    cursor cur is -- Noncompliant
      select 1 from dual;
  begin
    open blk.cur;
  end;
end;

create package pkg is
  cursor cur is -- compliant, this is a package specification, we don't know if there are any usages
    select 1 from dual;
end;

create package body pkg is
  cursor cur return custom_type is -- compliant, let's assume that cursors with return type are declared in package spec
    select 1 from dual;
end;
