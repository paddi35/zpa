declare
  foo number;
begin

  select count(*) -- Noncompliant {{Looks like there is a "foo" variable in this context. Review if this COUNT is correct.}}
--       ^^^^^^^^^^
    from tab;
    
  SELECT COUNT( * ), -- Noncompliant
         COUNT(*) -- Noncompliant
    INTO bar
    FROM tab;

  -- noncompliant code without a quick fix
  select count(distinct foo) -- Noncompliant
    from tab;

  -- don't report an error here, we don't have enough information to know if "bar" is a variable or a column of table "tab"
  select count(bar)
    from tab;

  select count(foo, bar), -- the COUNT function has only 1 parameter, so this isn't the Oracle built-in
         my_count(foo),
         pack.count(foo)
    from tab;
    
  bar := count(foo); -- the Oracle built-in can't be used here
end;

select count(foo) from dual; -- we don't have a scope here
