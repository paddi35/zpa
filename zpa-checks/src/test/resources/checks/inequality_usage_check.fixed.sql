begin
  foo := (x != a); -- Noncompliant {{Replace "<>" by "!=".}}
  foo := (x != a); -- Noncompliant {{Replace "^=" by "!=".}}
  foo := (x != a); -- Noncompliant {{Replace "~=" by "!=".}}
  foo := (x != a); -- Noncompliant {{Replace "<>" by "!=".}}
  foo := (x != a and y != b); -- Noncompliant
  -- Noncompliant@-1
  foo := (x
    != a); -- Noncompliant

  -- valid usage
  foo := (x != a);
end;
