begin
  -- noncompliant code
  var := (foo = null); -- Noncompliant {{Fix this comparison or change to "IS NULL".}}
--        ^^^^^^^^^^

  var := (foo = ''); -- Noncompliant
--        ^^^^^^^^

  var := (foo <> null); -- Noncompliant {{Fix this comparison or change to "IS NOT NULL".}}
--        ^^^^^^^^^^^

  var := (foo <> ''); -- Noncompliant
--        ^^^^^^^^^

  var := (null = foo); -- Noncompliant {{Fix this comparison or change to "IS NULL".}}
  var := (null != foo and '' ^= bar); -- Noncompliant
  -- Noncompliant@-1
  var := (FOO = NULL or NULL = BAR); -- Noncompliant
  -- Noncompliant@-1
  var := (null = a || b); -- Noncompliant
  var := (foo
    = null); -- Noncompliant @-1
  var := (null =
    foo); -- Noncompliant @-1

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
