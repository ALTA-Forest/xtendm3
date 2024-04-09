// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a Grades to EXTGRD
// Transaction AddLogGrade
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: GRAD - Grade
 * @param: GRNA - Name
 * 
*/


public class AddLogGrade extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Constructor 
  public AddLogGrade(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     int inCONO = program.LDAZD.CONO as Integer

     // Grade
     String inGRAD
     if (mi.in.get("GRAD") != null && mi.in.get("GRAD") != "") {
        inGRAD = mi.inData.get("GRAD").trim() 
     } else {
        inGRAD = ""         
     }
      
     // Name
     String inGRNA
     if (mi.in.get("GRNA") != null && mi.in.get("GRNA") != "") {
        inGRNA = mi.inData.get("GRNA").trim() 
     } else {
        inGRNA = ""        
     }
     

     // Validate grade record
     Optional<DBContainer> EXTGRD = findEXTGRD(inCONO, inGRAD)
     if(EXTGRD.isPresent()){
        mi.error("Log Grade already exists")   
        return             
     } else {
        // Write record 
        addEXTGRDRecord(inCONO, inGRAD, inGRNA)          
     }  

  }
  
    
  //******************************************************************** 
  // Get EXTGRD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTGRD(int CONO, String GRAD){  
     DBAction query = database.table("EXTGRD").index("00").build()
     DBContainer EXTGRD = query.getContainer()
     EXTGRD.set("EXCONO", CONO)
     EXTGRD.set("EXGRAD", GRAD)
     if(query.read(EXTGRD))  { 
       return Optional.of(EXTGRD)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTGRD record 
  //********************************************************************     
  void addEXTGRDRecord(int CONO, String GRAD, String GRNA){     
       DBAction action = database.table("EXTGRD").index("00").build()
       DBContainer EXTGRD = action.createContainer()
       EXTGRD.set("EXCONO", CONO)
       EXTGRD.set("EXGRAD", GRAD)
       EXTGRD.set("EXGRNA", GRNA) 
       EXTGRD.set("EXCHID", program.getUser())
       EXTGRD.set("EXCHNO", 1)           
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")          
       EXTGRD.set("EXRGDT", regdate) 
       EXTGRD.set("EXLMDT", regdate) 
       EXTGRD.set("EXRGTM", regtime)
       action.insert(EXTGRD)         
 } 

     
} 

