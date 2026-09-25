begin
  var := ''; -- Noncompliant {{Replace this empty string by NULL.}}
  rec.field := ''; -- Noncompliant
  VAR := ''; -- Noncompliant

  -- correct
  var := ' ';
  var := 'x';
  var := null;
end;
