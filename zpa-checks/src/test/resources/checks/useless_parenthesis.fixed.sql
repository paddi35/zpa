begin
  var := (x = 1); -- Noncompliant {{Remove those useless parenthesis.}}
--        ^^^^^^^
  var := (  a + b  ) * (c); -- Noncompliant
  -- Noncompliant@-1
  -- Noncompliant@-2
  var := (a
    + b); -- Noncompliant @-1

  -- noncompliant code without a quick fix
  var := ((a - b) day to second); -- Noncompliant

  -- valid
  var := (x = 1);
  var := ((x = 1) or (y = 2));
end;
