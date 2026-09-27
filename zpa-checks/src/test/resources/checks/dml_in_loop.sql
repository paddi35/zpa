begin
  loop
    insert into t values (1); -- Noncompliant {{Replace this DML statement and the enclosing loop with a single FORALL statement.}}
    exit when done;
  end loop;
end;
/
begin
  for i in 1..10 loop
    update t set x = 1 where id = i; -- Noncompliant
  end loop;
end;
/
begin
  while x < 10 loop
    delete from t where id = x; -- Noncompliant
    x := x + 1;
  end loop;
end;
/
begin
  loop
    merge into t target using dual source on (target.id = 1) when matched then update set target.x = 1; -- Noncompliant
    exit when done;
  end loop;
end;
/
begin
  loop
    if x = 1 then
      insert into t values (1); -- Noncompliant
    end if;
    exit when done;
  end loop;
end;
/
begin
  loop
    begin
      insert into t values (1); -- Noncompliant
    end;
    exit when done;
  end loop;
end;
/
begin
  loop
    forall i in 1..10
      insert into t values (tab(i)); -- ok, inside a FORALL statement
    exit when done;
  end loop;
end;
/
begin
  insert into t values (1); -- ok, not inside any loop
end;
/
begin
  loop
    execute immediate 'insert into t values (1)'; -- ok, dynamic SQL is not flagged
    exit when done;
  end loop;
end;
/
begin
  loop -- ok, no DML in the outer loop, only in the inner one
    loop
      insert into t values (1); -- Noncompliant
    end loop;
    exit when done;
  end loop;
end;
/
