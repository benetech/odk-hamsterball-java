package org.benetech.util;

import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;

public class RowUtils {

  public static Row resourceToRow(RowResource resource) {
    Row row = new Row();
    row.setCreateUser(resource.getCreateUser());
    row.setDataETagAtModification(resource.getDataETagAtModification());
    row.setFormId(resource.getFormId());
    row.setLastUpdateUser(resource.getLastUpdateUser());
    row.setLocale(resource.getLocale());
    row.setRowETag(resource.getRowETag());
    row.setRowFilterScope(resource.getRowFilterScope());
    row.setRowId(resource.getRowId());
    row.setSavepointCreator(resource.getSavepointCreator());
    row.setSavepointTimestamp(resource.getSavepointTimestamp());
    row.setSavepointType(resource.getSavepointType());
    row.setValues(resource.getValues());
    return row;
  }
}
