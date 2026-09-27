begin
  if foo then
    null;
  end if;
  
  if foo then
    null;
  elsif bar then
    null;
  end if;
  
  if a = 1 then
    x := 1;
  elsif a = 2 then
    x := 2;
  else
    x := 4;
  end if;

  -- the removed branch contains another duplicated condition
  if b then
    null;
  end if;

  if d then
    null;
  -- a comment before the branch is kept
  end if;

  IF e THEN NULL; END IF; -- Noncompliant

  -- noncompliant code without a quick fix: the function could return another value
  if f(x) then
    null;
  elsif f(x) then -- Noncompliant
    null;
  end if;

  -- correct
  if foo then
    null;
  end if;
  
  if foo then
    null;
  elsif bar then
    null;
  elsif baz then
    null;
  end if;
end;
/