begin
  var := null; -- Noncompliant {{Replace this empty string by NULL.}}
  rec.field := null; -- Noncompliant
  VAR := null; -- Noncompliant

  -- correct
  var := ' ';
  var := 'x';
  var := null;
end;
