begin
  var := (x in (1, 2)); -- Noncompliant {{Remove or fix the duplicated value "1" in the IN condition.}} [[secondary=2]]
--                 ^
  
  select col
    into var
    from tab
   where col in (x, y); -- Noncompliant {{Remove or fix the duplicated value "x" in the IN condition.}} [[secondary=8]]
--                     ^
   
  var := (x in (1, 2, 3)); -- Noncompliant
  -- Noncompliant@-1
  var := (x in (1)); -- Noncompliant
  -- Noncompliant@-1
  var := (x in (1)); -- Noncompliant
  var := (x in ('a',
                'b')); -- Noncompliant

  -- noncompliant code without a quick fix
  var := (x in (f(1), f(1))); -- Noncompliant
  var := (x in (1, /* again */ 1)); -- Noncompliant

  -- correct
  var := (x in (1, 2, 3));
end;