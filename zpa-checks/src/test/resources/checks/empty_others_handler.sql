begin
  do_something();
exception
  when others then -- Noncompliant {{Handle this exception (log it or re-raise it) instead of silently ignoring it.}}
    null;
end;
/
begin
  do_something();
exception
  when others then -- ok, re-raises
    raise;
end;
/
begin
  do_something();
exception
  when others then -- ok, logs the error
    log_error(sqlerrm);
end;
/
begin
  do_something();
exception
  when others then -- ok, more than just a NULL statement
    null;
    log_error(sqlerrm);
end;
/
begin
  do_something();
exception
  when my_exception or others then -- ok, combined designator, not a bare OTHERS
    null;
end;
/
begin
  do_something();
exception
  when my_exception then -- ok, named exception, not OTHERS at all
    null;
end;
/
