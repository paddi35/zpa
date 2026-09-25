begin
  declare -- Noncompliant {{Remove this DECLARE keyword.}}
  begin
    null;
  end;

  DECLARE BEGIN NULL; END; -- Noncompliant

  declare
  begin null; end; -- Noncompliant @-1

  declare
  begin
    declare begin null; end; -- Noncompliant
  end; -- Noncompliant @-3

  declare
    var number;
  begin
    null;
  end;
end;
