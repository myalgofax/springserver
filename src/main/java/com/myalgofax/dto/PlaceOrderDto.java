package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceOrderDto {
    
    	private String accessBrokerToken;
    	private String brokerCode;
    
        private String am;
        private String dq;
        private String es;
        private String mp;
        private String pc;
        private String pf;
        private String pr;
        private String pt;
        private String qt;
        private String rt;
        private String tp;
        private String ts;
        private String tt;
        
        // New fields from the second JSON
        private String bc = "1";          // Default value
        private String cf = "";           // Default empty
        private String dd = "NA";         // Default value
        private String mq = "0";          // Default value
        private String nc = "";           // Default empty
  
        private String pa = "BO:BO||CNC:CNC||CO:CO||NRML:NRML";  // Default value
        private String ot = "";           // Default empty
        private String tk = "";           // Default empty
        private String ur = "";           // Default empty
    
        private String cl = "0";          // Default value
        private String od = "";           // Default empty
        private String slv = "";          // Default empty
        private String sot = "";          // Default empty
        private String slt = "";          // Default empty
        private String sov = "";          // Default empty
        private String lat = "";          // Default empty
        private String tlt = "N";         // Default value
        private String tsv = "0";         // Default value
        private String ig = "";           // Default empty

        // Getters and Setters for all fields
        public String getAm() { return am; }
        public void setAm(String am) { this.am = am; }
        
        public String getDq() { return dq; }
        public void setDq(String dq) { this.dq = dq; }
        
        public String getEs() { return es; }
        public void setEs(String es) { this.es = es; }
        
        public String getMp() { return mp; }
        public void setMp(String mp) { this.mp = mp; }
        
        public String getPc() { return pc; }
        public void setPc(String pc) { this.pc = pc; }
        
        public String getPf() { return pf; }
        public void setPf(String pf) { this.pf = pf; }
        
        public String getPr() { return pr; }
        public void setPr(String pr) { this.pr = pr; }
        
        public String getPt() { return pt; }
        public void setPt(String pt) { this.pt = pt; }
        
        public String getQt() { return qt; }
        public void setQt(String qt) { this.qt = qt; }
        
        public String getRt() { return rt; }
        public void setRt(String rt) { this.rt = rt; }
        
        public String getTp() { return tp; }
        public void setTp(String tp) { this.tp = tp; }
        
        public String getTs() { return ts; }
        public void setTs(String ts) { this.ts = ts; }
        
        public String getTt() { return tt; }
        public void setTt(String tt) { this.tt = tt; }
        
        public String getBc() { return bc; }
        public void setBc(String bc) { this.bc = bc; }
        
        public String getCf() { return cf; }
        public void setCf(String cf) { this.cf = cf; }
        
        public String getDd() { return dd; }
        public void setDd(String dd) { this.dd = dd; }
        
        public String getMq() { return mq; }
        public void setMq(String mq) { this.mq = mq; }
        
        public String getNc() { return nc; }
        public void setNc(String nc) { this.nc = nc; }
        
     
        
        public String getPa() { return pa; }
        public void setPa(String pa) { this.pa = pa; }
        
        public String getOt() { return ot; }
        public void setOt(String ot) { this.ot = ot; }
        
        public String getTk() { return tk; }
        public void setTk(String tk) { this.tk = tk; }
        
        public String getUr() { return ur; }
        public void setUr(String ur) { this.ur = ur; }
   
        
        public String getCl() { return cl; }
        public void setCl(String cl) { this.cl = cl; }
        
        public String getOd() { return od; }
        public void setOd(String od) { this.od = od; }
        
        public String getSlv() { return slv; }
        public void setSlv(String slv) { this.slv = slv; }
        
        public String getSot() { return sot; }
        public void setSot(String sot) { this.sot = sot; }
        
        public String getSlt() { return slt; }
        public void setSlt(String slt) { this.slt = slt; }
        
        public String getSov() { return sov; }
        public void setSov(String sov) { this.sov = sov; }
        
        public String getLat() { return lat; }
        public void setLat(String lat) { this.lat = lat; }
        
        public String getTlt() { return tlt; }
        public void setTlt(String tlt) { this.tlt = tlt; }
        
        public String getTsv() { return tsv; }
        public void setTsv(String tsv) { this.tsv = tsv; }
        
        public String getIg() { return ig; }
        public void setIg(String ig) { this.ig = ig; }
		public String getAccessBrokerToken() {
			return accessBrokerToken;
		}
		public void setAccessBrokerToken(String accessBrokerToken) {
			this.accessBrokerToken = accessBrokerToken;
		}
		public String getBrokerCode() {
			return brokerCode;
		}
		public void setBrokerCode(String brokerCode) {
			this.brokerCode = brokerCode;
		}
        
        
    
}