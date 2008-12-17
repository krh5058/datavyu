require 'java'
require 'csv'
require 'time'

import 'au.com.nicta.openshapa.db.Database'
import 'au.com.nicta.openshapa.db.DataColumn'
import 'au.com.nicta.openshapa.db.MatrixVocabElement'
import 'au.com.nicta.openshapa.db.FloatDataValue'
import 'au.com.nicta.openshapa.db.DBElement'
import 'au.com.nicta.openshapa.db.TimeStamp'
import 'au.com.nicta.openshapa.db.DataCell'

begin
  # Create a data column, for our new
  col = DataColumn.new($db, "gait", MatrixVocabElement::MatrixType::FLOAT)
  $db.add_column(col)

  last_time = nil
  data = Hash.new

  # Parse the CSV data file.
  parsed_file = CSV::Reader.parse(File.open("C:\\NICTA\\openshapa\\src\\main\\samples\\scripts\\import_data.txt"))  

  # For each row in the parsed CSV value, inspect the values and store the
  # needed data.
  parsed_file.each do |row|
    # Parse the time stamp for the current row (the 5th element stored in the
    # CSV file.
    time = Time.parse(row[5]).to_i * 1000

    # Create a data sample - only if we have a unique timestamp.
    if last_time != time
      last_time = time
      data[time] = row[7]
      puts "[#{time} => #{row[7]}]"
    end
  end

  puts "***"

  col = $db.data_columns[0]
  mve = $db.get_matrix_ve(col.its_mve_id)

  puts col.get_id
  puts mve.get_id

  origin = nil
  # Sort the data we pulled from the CSV file by time, and for each data point
  # collected, create a cell in the OpenSHAPA database.
  data.sort.each do |key, value|
    if origin == nil
      origin = key
    end

    puts "[#{key} => #{value}]"
    # Create the OpenSHAPA time stamp identifiying the data point.
    #timestamp =
    cell = DataCell.new($db, col.get_id, mve.get_id)
    cell.onset = TimeStamp.new(1000, (key - origin))
    $db.append_cell(cell)
    puts cell
    #DataCell cell = new DataCell(db, dc.getID(), mve.getID());    
    #puts timestamp
  end
  puts "end"

rescue SystemErrorException => e
  puts "SystemErrorException: #{e.message}"
end