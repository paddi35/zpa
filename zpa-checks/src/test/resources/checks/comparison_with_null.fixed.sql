begin
  -- noncompliant code
  var := (foo is null); -- Noncompliant {{Fix this comparison or change to "IS NULL".}}
--        ^^^^^^^^^^

  var := (foo is null); -- Noncompliant
--        ^^^^^^^^

  var := (foo is not null); -- Noncompliant {{Fix this comparison or change to "IS NOT NULL".}}
--        ^^^^^^^^^^^

  var := (foo is not null); -- Noncompliant
--        ^^^^^^^^^

  var := (foo is null); -- Noncompliant {{Fix this comparison or change to "IS NULL".}}
  var := (foo is not null and bar is not null); -- Noncompliant
  -- Noncompliant@-1
  var := (FOO IS NULL or BAR IS NULL); -- Noncompliant
  -- Noncompliant@-1
  var := (a || b is null); -- Noncompliant
  var := (foo
    is null); -- Noncompliant @-1
  var := (foo is null); -- Noncompliant @-1

  -- noncompliant code without a quick fix
  var := (foo < null); -- Noncompliant
  var := (foo = /* comment */ null); -- Noncompliant
  var := (null = null); -- Noncompliant
  -- Noncompliant@-1

  -- valid code
  var := (foo is null);
  var := (foo is not null);
  var := (foo = 'x');
  var := (foo <> 'x');
  var := (foo = 1);
end;
