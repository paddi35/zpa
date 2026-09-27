begin
  return;
end;
/

begin
  raise_application_error(-20999, 'Custom error message');
end;
/

begin
  raise;
end;
/

begin
  for i in 1..10 loop
      continue;
  end loop;
end;
/

begin
  for i in 1..10 loop
      exit;
  end loop;
end;
/

begin
  begin
    return;
  end;
end;
/

begin
  begin
    begin
      return;
    end;
  end;
end;
/

begin
  return;
end;
/

begin
  if x then
    return;
    -- Noncompliant@+1
  end if;
  c := 3;
end;
/

begin
  return;
end;
/

begin
  return;
end;
/

begin
  null;
exception
  when others then
    raise;
end;
/

-- noncompliant code without a quick fix: a GOTO could jump to the label
begin
  goto skip;
  return;
  <<skip>> a := 1; -- Noncompliant
end;
/

-- correct
begin
  return;
end;
/

begin
  raise;
end;
/

begin
  raise_application_error(-20999, 'Custom error message');
end;
/

begin
  for i in 1..10 loop
      continue when i = 5;
      var := 1;
  end loop;
end;
/

begin
  for i in 1..10 loop
    exit when i = 5;
    var := 1;
  end loop;
end;
/

begin
  begin
    return;
  exception
    when others then
      null;
  end;
  
  var := 1;
end;
/

begin
  begin
    begin
      return;
    end;
  exception
    when others then
      null;
  end;
  
  var := 1; -- violation
end;
/
