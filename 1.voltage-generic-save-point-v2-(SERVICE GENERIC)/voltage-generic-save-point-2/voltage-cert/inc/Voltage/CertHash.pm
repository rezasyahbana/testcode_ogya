#!/usr/bin/perl
#
# Copyright (c) 2013 Voltage Security, Inc.
#

package Voltage::CertHash;

use strict;
use warnings;
use Crypt::X509;
use Crypt::X509::CRL;
use Digest::SHA::PurePerl qw ( sha1_hex );
use VOMS::Lite::PEMHelper qw( readCert );

require Exporter;
use vars qw(@EXPORT_OK @ISA @EXPORT %EXPORT_TAGS);
@ISA = qw(Exporter);
%EXPORT_TAGS = ( );
@EXPORT_OK = qw( certSubjectHash certFingerprint crlIssuerHash crlFingerprint );
@EXPORT = ( );

#
# Input: File name of X.509 certificate (PEM format)
# Output: 20-char hex string of cert fingerprint (SHA-1)
#
sub certFingerprint {
  my $file = shift;

  my $cert = readCert( $file );
  my $hexhash = sha1_hex($cert);

  return $hexhash;
}

#
# Input: File name of X.509 CRL (PEM format)
# Output: 20-char hex string of CRL fingerprint (SHA-1)
#
sub crlFingerprint {
  my $file = shift;

  my $crl = readCert( $file, "X509 CRL" );
  my $hexhash = sha1_hex($crl);

  return $hexhash;
}

#
# Input: File name of X.509 certificate (PEM format)
# Output: 8-char hex string of subject name hash
#
sub certSubjectHash {
  my $file = shift;

  # Assume the cert is PEM format (do we want to handle DER?)
  #my $cert = loadDER( $file );
  my $cert = readCert( $file );

  # Parse the loaded cert
  my $decoded = Crypt::X509->new( cert => $cert );

  return nameHash(@{$decoded->Subject});
}

#
# Input: File name of X.509 CRL (PEM format)
# Output: 8-char hex string of issuer name hash
#
sub crlIssuerHash {
  my $file = shift;

  # Assume the CRL is PEM format (do we want to handle DER?)
  #my $crl = loadDER( $file );
  my $crl = readCert( $file, "X509 CRL" );

  # Parse the loaded CRL
  my $decoded = Crypt::X509::CRL->new( crl => $crl );

  return nameHash(@{$decoded->Issuer});
}

sub nameHash {
  # Build the canonical encoding field-by-field from the parsed Subject Name
  my $canon = "";
  my $field;
  foreach $field (@_) {
    #print $field."\n";

    my $eqindex = index($field, '=');
    if($eqindex < 0) {
      warn "Invalid field: ".$field;
      next;
    }

    my $attrstr = substr $field, 0, $eqindex;
    my $val = substr $field, ($eqindex+1);

    # Convert field values from uppercase to lowercase
    $val =~ tr/A-Z/a-z/;

    # Collapse multiple spaces into a single space
    $val =~ s/\s+/ /g;

    # Trim leading and trailing spaces
    $val =~ s/^\s//;
    $val =~ s/\s$//;

    # Convert to UTF-8
    utf8::encode($val);

    my $oid;
    if($attrstr eq "C") { $oid = "550406"; }
    elsif($attrstr eq "S") { $oid = "550408"; }
    elsif($attrstr eq "l") { $oid = "550407"; }
    elsif($attrstr eq "O") { $oid = "55040a"; }
    elsif($attrstr eq "OU") { $oid = "55040b"; }
    elsif($attrstr eq "CN") { $oid = "550403"; }
    elsif($attrstr eq "E") { $oid = "2a864886f70d010901"; }
    elsif($attrstr eq "DC") { $oid = "0992268993f22c640119"; }
    elsif($attrstr eq "UID") { $oid = "0992268993f22c640101"; }
    elsif($attrstr eq "nameDistinguisher") { $oid = "028206010a0714"; }
    elsif($attrstr =~ m/^\d/) { $oid = oid2hex($attrstr); }
    else {
      warn "Unknown attribute: ".$attrstr;
      next;
    }

    # Re-encode the OID
    my $inner = "06".sprintf("%02x", length($oid)/2);
    $inner .= $oid;

    # Encode the value
    my $tag = "0c";
    $inner .= $tag;
    $inner .= sprintf("%02x", length($val));
    $inner .= unpack("H*", $val);

    # Assume every attr is a SET of SEQUENCE, and encode
    my $seq = "30".sprintf("%02x", length($inner)/2).$inner;
    $canon .= "31".sprintf("%02x", length($seq)/2).$seq;

    #print unpack("H*", $field)."\n";
  }

  my $binenc = pack("H*", $canon);
  #print unpack("H*", $binenc)."\n";

  my $hexhash = sha1_hex($binenc);
  my $ret = substr $hexhash, 6, 2;
  $ret .= substr $hexhash, 4, 2;
  $ret .= substr $hexhash, 2, 2;
  $ret .= substr $hexhash, 0, 2;

  return $ret;
}

sub oid2hex {
  my @arr = split(/\./, shift);
  my $b1 = 40 * shift(@arr);
  $b1 += shift(@arr);
  my $ret = sprintf("%02x", $b1);
  my $num;
  foreach $num (@arr) {
    if($num < 128) {
      $ret .= sprintf("%02x", $num);
    }
    elsif($num < 16384) {
      $ret .= sprintf("%02x%02x", ($num >> 7) | 128, $num & 127);
    }
    elsif($num < 2097152) {
      $ret .= sprintf("%02x%02x%02x", ($num >> 14) | 128, (($num >> 7) & 127) | 128, $num & 127);
    }
    else {
      $ret .= sprintf("%02x%02x%02x%02x", ($num >> 21) | 128, (($num >> 14) & 127 ) | 128, (($num >> 7) & 127) | 128, $num & 127);
    }
  }
  return $ret;
}

# Only works for DER certs...
sub loadDER {
        my $file = shift;
        open FILE, $file || die "cannot load certificate" . $file . "\n";
        binmode FILE;    # HELLO Windows, dont fuss with this
        my $holdTerminator = $/;
        undef $/;        # using slurp mode to read the DER-encoded binary cer
        my $cert = <FILE>;
        $/ = $holdTerminator;
        close FILE;
        return $cert;
}

1;
__END__
