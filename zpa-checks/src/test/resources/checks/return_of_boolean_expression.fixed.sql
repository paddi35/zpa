begin
  -- Noncompliant@+1 {{Replace this if-then-else statement by a single return statement.}}
  return (a = b);

  -- Noncompliant@+1
  return not (a = b);

  -- Noncompliant@+1
  return not (a = b and c = d);

  -- Noncompliant@+1
  return a = b or c = d;

  -- Noncompliant@+1
  return not (a = b or c = d);

  -- Noncompliant@+1
  return a;

  -- Noncompliant@+1
  return not   a;

  -- Noncompliant@+1
  return not (x is null);

  -- Noncompliant@+1
  RETURN x > 0
     AND y > 0;

  -- Noncompliant@+1
  <<check_x>>
  return x > 0;

  -- Noncompliant@+1
  return not names.exists(x);

  -- noncompliant code without a quick fix: a comment would be deleted
  if (a = b) then -- Noncompliant
    return true;
  else
    return false;
  end if;

  -- correct
  if (a = b) then
    return true;
  elsif (a = c) then
    return false;
  else
    return true;
  end if;

  if (a = b) then
    return true;
  else
    return true;
  end if;

  if (a = b) then
    return foo;
  else
    return false;
  end if;

  if (a = b) then
    a := 1;
    return true;
  else
    return false;
  end if;

  if (a = b) then
    return true;
  else
    a := 1;
    return false;
  end if;
end;
