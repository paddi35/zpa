create procedure foo(bar in varchar2 default null) is -- parameter declaration, no issue
  var1 varchar2(1); -- Noncompliant {{Remove this unnecessary initialization to NULL.}}
  var2 varchar2(1); -- Noncompliant
  var3 varchar2(1); -- Noncompliant
  var4 varchar2(1); -- Noncompliant
  var5 varchar2(1) null; -- Noncompliant
  var6 varchar2(1); -- Noncompliant

  type rec is record (field varchar2(1), field2 number); -- Noncompliant
  -- Noncompliant@-1

  -- noncompliant code without a quick fix
  const1 constant varchar2(1) := null; -- Noncompliant
  var7 varchar2(1) not null := null; -- Noncompliant
  var8 varchar2(1) := /* comment */ null; -- Noncompliant

  cursor cur(param in varchar2 default null) is -- cursor parameter, no issue
    select 1 from dual;
begin
  null;
end;
