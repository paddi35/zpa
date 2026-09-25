begin
   -- Noncompliant {{Remove this DECLARE keyword.}}
  begin
    null;
  end;

  BEGIN NULL; END; -- Noncompliant

  begin null; end; -- Noncompliant @-1

  begin
    begin null; end; -- Noncompliant
  end; -- Noncompliant @-3

  declare
    var number;
  begin
    null;
  end;
end;
